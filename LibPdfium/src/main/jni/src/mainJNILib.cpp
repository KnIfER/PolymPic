#include "util.hpp"

extern "C" {
    #include <unistd.h>
    #include <sys/mman.h>
    #include <sys/stat.h>
    #include <string.h>
    #include <stdio.h>
}

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/bitmap.h>
#include <utils/Mutex.h>
using namespace android;

#include <fpdfview.h>
#include <fpdf_doc.h>
#include <fpdf_text.h>
#include <fpdf_annot.h>
#include <fpdf_save.h>
#include <string>
#include <vector>

static Mutex sLibraryLock;

static int sLibraryReferenceCount = 0;

static void initLibraryIfNeed(){
    Mutex::Autolock lock(sLibraryLock);
    if(sLibraryReferenceCount == 0){
        LOGD("Init FPDF library");
        FPDF_InitLibrary();
    }
    sLibraryReferenceCount++;
}

static void destroyLibraryIfNeed(){
    Mutex::Autolock lock(sLibraryLock);
    sLibraryReferenceCount--;
    if(sLibraryReferenceCount == 0){
        LOGD("Destroy FPDF library");
        FPDF_DestroyLibrary();
    }
}

struct rgb {
    uint8_t red;
    uint8_t green;
    uint8_t blue;
};

class DocumentFile {
    public:
    int fileFd;
    FPDF_DOCUMENT pdfDocument = NULL;
    size_t fileSize;

    DocumentFile() { initLibraryIfNeed(); }
    ~DocumentFile();
};

DocumentFile::~DocumentFile(){
    if(pdfDocument != NULL){
        FPDF_CloseDocument(pdfDocument);
    }

    //destroyLibraryIfNeed();
}

template <class string_type>
inline typename string_type::value_type* WriteInto(string_type* str, size_t length_with_null) {
  str->reserve(length_with_null);
  str->resize(length_with_null - 1);
  return &((*str)[0]);
}

inline long getFileSize(int fd){
    struct stat file_state;

    if(fstat(fd, &file_state) >= 0){
        return (long)(file_state.st_size);
    }else{
        LOGE("Error getting file size");
        return 0;
    }
}

static char* getErrorDescription(const long error) {
    char* description = NULL;
    switch(error) {
        case FPDF_ERR_SUCCESS:
            asprintf(&description, "No error.");
            break;
        case FPDF_ERR_FILE:
            asprintf(&description, "File not found or could not be opened.");
            break;
        case FPDF_ERR_FORMAT:
            asprintf(&description, "File not in PDF format or corrupted.");
            break;
        case FPDF_ERR_PASSWORD:
            asprintf(&description, "Incorrect password.");
            break;
        case FPDF_ERR_SECURITY:
            asprintf(&description, "Unsupported security scheme.");
            break;
        case FPDF_ERR_PAGE:
            asprintf(&description, "Page not found or content error.");
            break;
        default:
            asprintf(&description, "Unknown error.");
    }

    return description;
}

int jniThrowException(JNIEnv* env, const char* className, const char* message) {
    jclass exClass = env->FindClass(className);
    if (exClass == NULL) {
        LOGE("Unable to find exception class %s", className);
        return -1;
    }

    if(env->ThrowNew(exClass, message ) != JNI_OK) {
        LOGE("Failed throwing '%s' '%s'", className, message);
        return -1;
    }

    return 0;
}

int jniThrowExceptionFmt(JNIEnv* env, const char* className, const char* fmt, ...) {
    va_list args;
    va_start(args, fmt);
    char msgBuf[512];
    vsnprintf(msgBuf, sizeof(msgBuf), fmt, args);
    return jniThrowException(env, className, msgBuf);
    va_end(args);
}

jobject NewLong(JNIEnv* env, jlong value) {
    jclass cls = env->FindClass("java/lang/Long");
    jmethodID methodID = env->GetMethodID(cls, "<init>", "(J)V");
    return env->NewObject(cls, methodID, value);
}

jobject NewInteger(JNIEnv* env, jint value) {
    jclass cls = env->FindClass("java/lang/Integer");
    jmethodID methodID = env->GetMethodID(cls, "<init>", "(I)V");
    return env->NewObject(cls, methodID, value);
}

uint16_t rgbTo565(rgb *color) {
    return ((color->red >> 3) << 11) | ((color->green >> 2) << 5) | (color->blue >> 3);
}

void rgbBitmapTo565(void *source, int sourceStride, void *dest, AndroidBitmapInfo *info) {
    rgb *srcLine;
    uint16_t *dstLine;
    int y, x;
    for (y = 0; y < info->height; y++) {
        srcLine = (rgb*) source;
        dstLine = (uint16_t*) dest;
        for (x = 0; x < info->width; x++) {
            dstLine[x] = rgbTo565(&srcLine[x]);
        }
        source = (char*) source + sourceStride;
        dest = (char*) dest + info->stride;
    }
}

extern "C" { //For JNI support

static int getBlock(void* param, unsigned long position, unsigned char* outBuffer,
        unsigned long size) {
    const int fd = reinterpret_cast<intptr_t>(param);
    const int readCount = pread(fd, outBuffer, size, position);
    if (readCount < 0) {
        LOGE("Cannot read from file descriptor. Error:%d", errno);
        return 0;
    }
    return 1;
}

JNI_FUNC(jlong, PdfiumCore, nativeOpenDocument)(JNI_ARGS, jint fd, jstring password){

    size_t fileLength = (size_t)getFileSize(fd);
    if(fileLength <= 0) {
        jniThrowException(env, "java/io/IOException",
                                    "File is empty");
        return -1;
    }

    DocumentFile *docFile = new DocumentFile();

    FPDF_FILEACCESS loader;
    loader.m_FileLen = fileLength;
    loader.m_Param = reinterpret_cast<void*>(intptr_t(fd));
    loader.m_GetBlock = &getBlock;

    const char *cpassword = NULL;
    if(password != NULL) {
        cpassword = env->GetStringUTFChars(password, NULL);
    }

    FPDF_DOCUMENT document = FPDF_LoadCustomDocument(&loader, cpassword);

    if(cpassword != NULL) {
        env->ReleaseStringUTFChars(password, cpassword);
    }

    if (!document) {
        delete docFile;

        const long errorNum = FPDF_GetLastError();
        if(errorNum == FPDF_ERR_PASSWORD) {
            jniThrowException(env, "com/shockwave/pdfium/PdfPasswordException",
                                    "Password required or incorrect password.");
        } else {
            char* error = getErrorDescription(errorNum);
            jniThrowExceptionFmt(env, "java/io/IOException",
                                    "cannot create document: %s", error);

            free(error);
        }

        return -1;
    }

    docFile->pdfDocument = document;

    return reinterpret_cast<jlong>(docFile);
}

JNI_FUNC(jlong, PdfiumCore, nativeOpenMemDocument)(JNI_ARGS, jbyteArray data, jstring password){
    DocumentFile *docFile = new DocumentFile();

    const char *cpassword = NULL;
    if(password != NULL) {
        cpassword = env->GetStringUTFChars(password, NULL);
    }

    jbyte *cData = env->GetByteArrayElements(data, NULL);
    int size = (int) env->GetArrayLength(data);
    jbyte *cDataCopy = new jbyte[size];
    memcpy(cDataCopy, cData, size);
    FPDF_DOCUMENT document = FPDF_LoadMemDocument( reinterpret_cast<const void*>(cDataCopy),
                                                          size, cpassword);
    env->ReleaseByteArrayElements(data, cData, JNI_ABORT);

    if(cpassword != NULL) {
        env->ReleaseStringUTFChars(password, cpassword);
    }

    if (!document) {
        delete docFile;

        const long errorNum = FPDF_GetLastError();
        if(errorNum == FPDF_ERR_PASSWORD) {
            jniThrowException(env, "com/shockwave/pdfium/PdfPasswordException",
                                    "Password required or incorrect password.");
        } else {
            char* error = getErrorDescription(errorNum);
            jniThrowExceptionFmt(env, "java/io/IOException",
                                    "cannot create document: %s", error);

            free(error);
        }

        return -1;
    }

    docFile->pdfDocument = document;

    return reinterpret_cast<jlong>(docFile);
}

JNI_FUNC(jint, PdfiumCore, nativeGetPageCount)(JNI_ARGS, jlong documentPtr){
    DocumentFile *doc = reinterpret_cast<DocumentFile*>(documentPtr);
    return (jint)FPDF_GetPageCount(doc->pdfDocument);
}

JNI_FUNC(void, PdfiumCore, nativeCloseDocument)(JNI_ARGS, jlong documentPtr){
    DocumentFile *doc = reinterpret_cast<DocumentFile*>(documentPtr);
    FPDF_CloseDocument((FPDF_DOCUMENT)doc->pdfDocument);
    delete doc;
}

static jlong loadPageInternal(JNIEnv *env, DocumentFile *doc, int pageIndex){
    try{
        if(doc == NULL) throw "Get page document null";

        FPDF_DOCUMENT pdfDoc = doc->pdfDocument;
        if(pdfDoc != NULL){
            FPDF_PAGE page = FPDF_LoadPage(pdfDoc, pageIndex);
            if (page == NULL) {
                throw "Loaded page is null";
            }
            return reinterpret_cast<jlong>(page);
        }else{
            throw "Get page pdf document null";
        }

    }catch(const char *msg){
        LOGE("%s", msg);

        jniThrowException(env, "java/lang/IllegalStateException",
                                "cannot load page");

        return -1;
    }
}

static jlong loadTextPageInternal(JNIEnv *env, FPDF_PAGE page){
    try{
        FPDF_TEXTPAGE text = FPDFText_LoadPage(page);
        if (page == NULL) {
            throw "Loaded page is null";
        }
        return reinterpret_cast<jlong>(text);
    } catch(const char *msg) {
        LOGE("%s", msg);

        jniThrowException(env, "java/lang/IllegalStateException",
                                "cannot load text");

        return -1;
    }
}

static void closePageInternal(jlong pagePtr) {
    FPDF_ClosePage(reinterpret_cast<FPDF_PAGE>(pagePtr));
}

JNI_FUNC(jlong, PdfiumCore, nativeLoadPage)(JNI_ARGS, jlong docPtr, jint pageIndex){
    DocumentFile *doc = reinterpret_cast<DocumentFile*>(docPtr);
    return loadPageInternal(env, doc, (int)pageIndex);
}

JNI_FUNC(jlongArray, PdfiumCore, nativeLoadPages)(JNI_ARGS, jlong docPtr, jint fromIndex, jint toIndex){
    DocumentFile *doc = reinterpret_cast<DocumentFile*>(docPtr);

    if(toIndex < fromIndex) return NULL;
    jlong pages[ toIndex - fromIndex + 1 ];

    int i;
    for(i = 0; i <= (toIndex - fromIndex); i++){
        pages[i] = loadPageInternal(env, doc, (int)(i + fromIndex));
    }

    jlongArray javaPages = env -> NewLongArray( (jsize)(toIndex - fromIndex + 1) );
    env -> SetLongArrayRegion(javaPages, 0, (jsize)(toIndex - fromIndex + 1), (const jlong*)pages);

    return javaPages;
}

JNI_FUNC(jlong, PdfiumCore, nativeLoadTextPage)(JNI_ARGS, jlong pagePtr){
    return loadTextPageInternal(env, (FPDF_PAGE)pagePtr);
}

JNI_FUNC(jint, PdfiumCore, nativeGetCharIndexAtPos)(JNI_ARGS, jlong textPtr, jdouble posX, jdouble posY, jdouble tolX, jdouble tolY){
    return FPDFText_GetCharIndexAtPos((FPDF_TEXTPAGE)textPtr, posX, posY, tolX, tolY);
}

JNI_FUNC(jint, PdfiumCore, nativeGetCharIndexAtCoord)(JNI_ARGS, jlong pagePtr, jdouble width, jdouble height, jlong textPtr, jdouble posX, jdouble posY, jdouble tolX, jdouble tolY){
    double px, py;
    FPDF_DeviceToPage((FPDF_PAGE)pagePtr, 0, 0, width, height, 0, posX, posY, &px, &py);
    return FPDFText_GetCharIndexAtPos((FPDF_TEXTPAGE)textPtr, px, py, tolX, tolY);
}

JNI_FUNC(jint, PdfiumCore, nativeGetTexts)(JNI_ARGS, jlong textPtr, jdouble posX, jdouble posY, jdouble tolX, jdouble tolY){
    return FPDFText_GetCharIndexAtPos((FPDF_TEXTPAGE)textPtr, posX, posY, tolX, tolY);
}

JNI_FUNC(jint, PdfiumCore, nativeGetTextLength)(JNI_ARGS, jlong textPtr){
    return FPDFText_CountChars((FPDF_TEXTPAGE)textPtr);
}

JNI_FUNC(jstring, PdfiumCore, nativeGetText)(JNI_ARGS, jlong textPtr) {
    int len = FPDFText_CountChars((FPDF_TEXTPAGE)textPtr);
    //unsigned short* buffer = malloc(len*sizeof(unsigned short));
    unsigned short* buffer = new unsigned short[len+1];
    FPDFText_GetText((FPDF_TEXTPAGE)textPtr, 0, len, buffer);
    jstring ret = env->NewString(buffer, len);
    delete []buffer;
    return ret;
}

JNI_FUNC(jint, PdfiumCore, nativeCountAndGetRects)(JNI_ARGS, jlong pagePtr, jint offsetY, jint offsetX, jint width, jint height, jobject arr, jlong textPtr, jint st, jint ed) {
    jclass arrList = env->FindClass("java/util/ArrayList");
    jmethodID arrList_add = env->GetMethodID(arrList,"add","(Ljava/lang/Object;)Z");
    jmethodID arrList_get = env->GetMethodID(arrList,"get","(I)Ljava/lang/Object;");
    jmethodID arrList_size = env->GetMethodID(arrList,"size","()I");
    jmethodID arrList_enssurecap = env->GetMethodID(arrList,"ensureCapacity","(I)V");

    jclass rectF = env->FindClass("android/graphics/RectF");
    jmethodID rectF_ = env->GetMethodID(rectF, "<init>", "(FFFF)V");
    jmethodID rectF_set = env->GetMethodID(rectF, "set", "(FFFF)V");

    int rectCount = FPDFText_CountRects((FPDF_TEXTPAGE)textPtr, (int)st, (int)ed);
    env->CallVoidMethod( arr, arrList_enssurecap, rectCount);
    double left, top, right, bottom;
    int arraySize = env->CallIntMethod(arr, arrList_size);
    int deviceX, deviceY;
    for(int i=0;i<rectCount;i++) {
        if(FPDFText_GetRect((FPDF_TEXTPAGE)textPtr, i, &left, &top, &right, &bottom)) {
            FPDF_PageToDevice((FPDF_PAGE)pagePtr, 0, 0, width, height, 0, left, top, &deviceX, &deviceY);
            int width = right-left;
            int height = top-bottom;
            left=deviceX+offsetX;
            top=deviceY+offsetY;
            right=left+width;
            bottom=top+height;
            if(i>=arraySize) {
                env->CallBooleanMethod(arr
                    , arrList_add
                    , env->NewObject(rectF, rectF_, left, top, right, bottom) );
            } else {
                jobject rI = env->CallObjectMethod(arr, arrList_get, i);
                env->CallVoidMethod(rI, rectF_set, left, top, right, bottom);
            }
        }
    }
    return rectCount;
}

JNI_FUNC(void, PdfiumCore, nativeGetCharPos)(JNI_ARGS, jlong pagePtr, jint offsetY, jint offsetX, jint width, jint height, jobject pt, jlong textPtr, jint idx, jboolean loose) {
    //jclass point = env->FindClass("android/graphics/PointF");
    //jmethodID point_set = env->GetMethodID(point,"set","(FF)V");
    jclass rectF = env->FindClass("android/graphics/RectF");
    jmethodID rectF_ = env->GetMethodID(rectF, "<init>", "(FFFF)V");
    jmethodID rectF_set = env->GetMethodID(rectF, "set", "(FFFF)V");
    double left, top, right, bottom;
    if(loose) {
        FS_RECTF res={0};
        if(!FPDFText_GetLooseCharBox((FPDF_TEXTPAGE)textPtr, idx, &res)) {
            return;
        }
        left=res.left;
        top=res.top;
        right=res.right;
        bottom=res.bottom;
    } else {
        if(!FPDFText_GetCharBox((FPDF_TEXTPAGE)textPtr, idx, &left, &right, &bottom, &top)) {
            return;
        }
    }
    int deviceX, deviceY;
    FPDF_PageToDevice((FPDF_PAGE)pagePtr, 0, 0, width, height, 0, left, top, &deviceX, &deviceY);
    width = right-left;
    height = top-bottom;
    left=deviceX+offsetX;
    top=deviceY+offsetY;
    right=left+width;
    bottom=top+height;
    //env->CallVoidMethod(pt, point_set, left, top);
    env->CallVoidMethod(pt, rectF_set, left, top, right, bottom);
}

JNI_FUNC(jboolean, PdfiumCore, nativeGetMixedLooseCharPos)(JNI_ARGS, jlong pagePtr, jint offsetY, jint offsetX, jint width, jint height, jobject pt, jlong textPtr, jint idx, jboolean loose) {
    jclass rectF = env->FindClass("android/graphics/RectF");
    jmethodID rectF_ = env->GetMethodID(rectF, "<init>", "(FFFF)V");
    jmethodID rectF_set = env->GetMethodID(rectF, "set", "(FFFF)V");
    double left, top, right, bottom;
    if(!FPDFText_GetCharBox((FPDF_TEXTPAGE)textPtr, idx, &left, &right, &bottom, &top)) {
            return false;
    }
    FS_RECTF res={0};
    if(!FPDFText_GetLooseCharBox((FPDF_TEXTPAGE)textPtr, idx, &res)) {
        return false;
    }
    top=fmax(res.top, top);
    bottom=fmin(res.bottom, bottom);
    left=fmin(res.left, left);
    right=fmax(res.right, right);
    int deviceX, deviceY;
    FPDF_PageToDevice((FPDF_PAGE)pagePtr, 0, 0, width, height, 0, left, top, &deviceX, &deviceY);
    width = right-left;
    height = top-bottom;
    top=deviceY+offsetY;
    left=deviceX+offsetX;
    right=left+width;
    bottom=top+height;
    env->CallVoidMethod(pt, rectF_set, left, top, right, bottom);
    return true;
}

//JNI_FUNC(void, PdfiumCore, nativeClosePage)(JNI_ARGS, jlong pagePtr){
//    closePageInternal(pagePtr);
//}

JNI_FUNC(void, PdfiumCore, nativeClosePage)(JNI_ARGS, jlong pagePtr){
    FPDF_ClosePage((FPDF_PAGE)pagePtr);
}

JNI_FUNC(void, PdfiumCore, nativeClosePages)(JNI_ARGS, jlongArray pagesPtr){
    int length = (int)(env -> GetArrayLength(pagesPtr));
    jlong *pages = env -> GetLongArrayElements(pagesPtr, NULL);

    int i;
    for(i = 0; i < length; i++){ closePageInternal(pages[i]); }
}

JNI_FUNC(void, PdfiumCore, nativeClosePageAndText)(JNI_ARGS, jlong pagePtr, jlong textPtr){
    FPDF_ClosePage((FPDF_PAGE)pagePtr);
    if(textPtr) {
        FPDFText_ClosePage((FPDF_TEXTPAGE)textPtr);
    }
}
JNI_FUNC(jint, PdfiumCore, nativeGetPageWidthPixel)(JNI_ARGS, jlong pagePtr, jint dpi){
    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);
    return (jint)(FPDF_GetPageWidth(page) * dpi / 72);
}
JNI_FUNC(jint, PdfiumCore, nativeGetPageHeightPixel)(JNI_ARGS, jlong pagePtr, jint dpi){
    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);
    return (jint)(FPDF_GetPageHeight(page) * dpi / 72);
}

JNI_FUNC(jint, PdfiumCore, nativeGetPageWidthPoint)(JNI_ARGS, jlong pagePtr){
    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);
    return (jint)FPDF_GetPageWidth(page);
}
JNI_FUNC(jint, PdfiumCore, nativeGetPageHeightPoint)(JNI_ARGS, jlong pagePtr){
    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);
    return (jint)FPDF_GetPageHeight(page);
}
JNI_FUNC(jobject, PdfiumCore, nativeGetPageSizeByIndex)(JNI_ARGS, jlong docPtr, jint pageIndex, jint dpi){
    DocumentFile *doc = reinterpret_cast<DocumentFile*>(docPtr);
    if(doc == NULL) {
        LOGE("Document is null");

        jniThrowException(env, "java/lang/IllegalStateException",
                               "Document is null");
        return NULL;
    }

    double width, height;
    int result = FPDF_GetPageSizeByIndex(doc->pdfDocument, pageIndex, &width, &height);

    if (result == 0) {
        width = 0;
        height = 0;
    }

    jint widthInt = (jint) width;
    jint heightInt = (jint) height;

    jclass clazz = env->FindClass("com/shockwave/pdfium/util/Size");
    jmethodID constructorID = env->GetMethodID(clazz, "<init>", "(II)V");
    return env->NewObject(clazz, constructorID, widthInt, heightInt);
}

static void renderPageInternal( FPDF_PAGE page,
                                ANativeWindow_Buffer *windowBuffer,
                                int startX, int startY,
                                int canvasHorSize, int canvasVerSize,
                                int drawSizeHor, int drawSizeVer,
                                bool renderAnnot){

    FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx( canvasHorSize, canvasVerSize,
                                                 FPDFBitmap_BGRA,
                                                 windowBuffer->bits, (int)(windowBuffer->stride) * 4);

    /*LOGD("Start X: %d", startX);
    LOGD("Start Y: %d", startY);
    LOGD("Canvas Hor: %d", canvasHorSize);
    LOGD("Canvas Ver: %d", canvasVerSize);
    LOGD("Draw Hor: %d", drawSizeHor);
    LOGD("Draw Ver: %d", drawSizeVer);*/

    if(drawSizeHor < canvasHorSize || drawSizeVer < canvasVerSize){
        FPDFBitmap_FillRect( pdfBitmap, 0, 0, canvasHorSize, canvasVerSize,
                             0x848484FF); //Gray
    }

    int baseHorSize = (canvasHorSize < drawSizeHor)? canvasHorSize : drawSizeHor;
    int baseVerSize = (canvasVerSize < drawSizeVer)? canvasVerSize : drawSizeVer;
    int baseX = (startX < 0)? 0 : startX;
    int baseY = (startY < 0)? 0 : startY;
    int flags = FPDF_REVERSE_BYTE_ORDER;

    if(renderAnnot) {
    	flags |= FPDF_ANNOT;
    }

    FPDFBitmap_FillRect( pdfBitmap, baseX, baseY, baseHorSize, baseVerSize,
                         0xFFFFFFFF); //White

    FPDF_RenderPageBitmap( pdfBitmap, page,
                           startX, startY,
                           drawSizeHor, drawSizeVer,
                           0, flags );
}

JNI_FUNC(void, PdfiumCore, nativeRenderPage)(JNI_ARGS, jlong pagePtr, jobject objSurface,
                                             jint dpi, jint startX, jint startY,
                                             jint drawSizeHor, jint drawSizeVer,
                                             jboolean renderAnnot){
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, objSurface);
    if(nativeWindow == NULL){
        LOGE("native window pointer null");
        return;
    }
    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);

    if(page == NULL || nativeWindow == NULL){
        LOGE("Render page pointers invalid");
        return;
    }

    if(ANativeWindow_getFormat(nativeWindow) != WINDOW_FORMAT_RGBA_8888){
        LOGD("Set format to RGBA_8888");
        ANativeWindow_setBuffersGeometry( nativeWindow,
                                          ANativeWindow_getWidth(nativeWindow),
                                          ANativeWindow_getHeight(nativeWindow),
                                          WINDOW_FORMAT_RGBA_8888 );
    }

    ANativeWindow_Buffer buffer;
    int ret;
    if( (ret = ANativeWindow_lock(nativeWindow, &buffer, NULL)) != 0 ){
        LOGE("Locking native window failed: %s", strerror(ret * -1));
        return;
    }

    renderPageInternal(page, &buffer,
                       (int)startX, (int)startY,
                       buffer.width, buffer.height,
                       (int)drawSizeHor, (int)drawSizeVer,
                       (bool)renderAnnot);

    ANativeWindow_unlockAndPost(nativeWindow);
    ANativeWindow_release(nativeWindow);
}

JNI_FUNC(void, PdfiumCore, nativeRenderPageBitmap)(JNI_ARGS, jlong pagePtr, jobject bitmap,
                                             jint dpi, jint startX, jint startY,
                                             jint drawSizeHor, jint drawSizeVer,
                                             jboolean renderAnnot){

    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);

    if(page == NULL || bitmap == NULL){
        LOGE("Render page pointers invalid");
        return;
    }

    AndroidBitmapInfo info;
    int ret;
    if((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("Fetching bitmap info failed: %s", strerror(ret * -1));
        return;
    }

    int canvasHorSize = info.width;
    int canvasVerSize = info.height;

    if(info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 && info.format != ANDROID_BITMAP_FORMAT_RGB_565){
        LOGE("Bitmap format must be RGBA_8888 or RGB_565");
        return;
    }

    void *addr;
    if( (ret = AndroidBitmap_lockPixels(env, bitmap, &addr)) != 0 ){
        LOGE("Locking bitmap failed: %s", strerror(ret * -1));
        return;
    }

    //LOGE("Locking bitmap: %d : %d", addr, info.format == ANDROID_BITMAP_FORMAT_RGB_565);

    void *tmp;
    int format;
    int sourceStride;
    if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
        tmp = malloc(canvasVerSize * canvasHorSize * sizeof(rgb));
        sourceStride = canvasHorSize * sizeof(rgb);
        format = FPDFBitmap_BGR;
    } else {
        tmp = addr;
        sourceStride = info.stride;
        format = FPDFBitmap_BGRA;
    }

    FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx( canvasHorSize, canvasVerSize, format, tmp, sourceStride);

    if(!pdfBitmap) return;
    /*LOGD("Start X: %d", startX);
    LOGD("Start Y: %d", startY);
    LOGD("Canvas Hor: %d", canvasHorSize);
    LOGD("Canvas Ver: %d", canvasVerSize);
    LOGD("Draw Hor: %d", drawSizeHor);
    LOGD("Draw Ver: %d", drawSizeVer);*/

    //if(drawSizeHor < canvasHorSize || drawSizeVer < canvasVerSize)
    //{
    //    FPDFBitmap_FillRect( pdfBitmap, 0, 0, canvasHorSize, canvasVerSize, 0x848484FF); //Gray
    //}

    int baseHorSize = (canvasHorSize < drawSizeHor)? canvasHorSize : (int)drawSizeHor;
    int baseVerSize = (canvasVerSize < drawSizeVer)? canvasVerSize : (int)drawSizeVer;
    int baseX = (startX < 0)? 0 : (int)startX;
    int baseY = (startY < 0)? 0 : (int)startY;
    int flags = FPDF_REVERSE_BYTE_ORDER;

    if(renderAnnot) {
    	flags |= FPDF_ANNOT;
    }

    //FPDFBitmap_FillRect( pdfBitmap, baseX, baseY, baseHorSize, baseVerSize, 0xFFFFFFFF); //White

    FPDF_RenderPageBitmap( pdfBitmap, page,
                           startX, startY,
                           (int)drawSizeHor, (int)drawSizeVer,
                           0, flags );

    if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
        rgbBitmapTo565(tmp, sourceStride, addr, &info);
        free(tmp);
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}

JNI_FUNC(jstring, PdfiumCore, nativeGetDocumentMetaText)(JNI_ARGS, jlong docPtr, jstring tag) {
    const char *ctag = env->GetStringUTFChars(tag, NULL);
    if (ctag == NULL) {
        return env->NewStringUTF("");
    }
    DocumentFile *doc = reinterpret_cast<DocumentFile*>(docPtr);

    size_t bufferLen = FPDF_GetMetaText(doc->pdfDocument, ctag, NULL, 0);
    if (bufferLen <= 2) {
        return env->NewStringUTF("");
    }
    std::wstring text;
    FPDF_GetMetaText(doc->pdfDocument, ctag, WriteInto(&text, bufferLen + 1), bufferLen);
    env->ReleaseStringUTFChars(tag, ctag);
    return env->NewString((jchar*) text.c_str(), bufferLen / 2 - 1);
}

JNI_FUNC(jobject, PdfiumCore, nativeGetFirstChildBookmark)(JNI_ARGS, jlong docPtr, jobject bookmarkPtr) {
    DocumentFile *doc = reinterpret_cast<DocumentFile*>(docPtr);
    FPDF_BOOKMARK parent;
    if(bookmarkPtr == NULL) {
        parent = NULL;
    } else {
        jclass longClass = env->GetObjectClass(bookmarkPtr);
        jmethodID longValueMethod = env->GetMethodID(longClass, "longValue", "()J");

        jlong ptr = env->CallLongMethod(bookmarkPtr, longValueMethod);
        parent = reinterpret_cast<FPDF_BOOKMARK>(ptr);
    }
    FPDF_BOOKMARK bookmark = FPDFBookmark_GetFirstChild(doc->pdfDocument, parent);
    if (bookmark == NULL) {
        return NULL;
    }
    return NewLong(env, reinterpret_cast<jlong>(bookmark));
}

JNI_FUNC(jobject, PdfiumCore, nativeGetSiblingBookmark)(JNI_ARGS, jlong docPtr, jlong bookmarkPtr) {
    DocumentFile *doc = reinterpret_cast<DocumentFile*>(docPtr);
    FPDF_BOOKMARK parent = reinterpret_cast<FPDF_BOOKMARK>(bookmarkPtr);
    FPDF_BOOKMARK bookmark = FPDFBookmark_GetNextSibling(doc->pdfDocument, parent);
    if (bookmark == NULL) {
        return NULL;
    }
    return NewLong(env, reinterpret_cast<jlong>(bookmark));
}

JNI_FUNC(jstring, PdfiumCore, nativeGetBookmarkTitle)(JNI_ARGS, jlong bookmarkPtr) {
    FPDF_BOOKMARK bookmark = reinterpret_cast<FPDF_BOOKMARK>(bookmarkPtr);
    size_t bufferLen = FPDFBookmark_GetTitle(bookmark, NULL, 0);
    if (bufferLen <= 2) {
        return env->NewStringUTF("");
    }
    std::wstring title;
    FPDFBookmark_GetTitle(bookmark, WriteInto(&title, bufferLen + 1), bufferLen);
    return env->NewString((jchar*) title.c_str(), bufferLen / 2 - 1);
}

JNI_FUNC(jlong, PdfiumCore, nativeGetBookmarkDestIndex)(JNI_ARGS, jlong docPtr, jlong bookmarkPtr) {
    DocumentFile *doc = reinterpret_cast<DocumentFile*>(docPtr);
    FPDF_BOOKMARK bookmark = reinterpret_cast<FPDF_BOOKMARK>(bookmarkPtr);

    FPDF_DEST dest = FPDFBookmark_GetDest(doc->pdfDocument, bookmark);
    if (dest == NULL) {
        return -1;
    }
    return (jlong) FPDFDest_GetDestPageIndex(doc->pdfDocument, dest);
}

JNI_FUNC(jint, PdfiumCore, nativeCountAnnot)(JNI_ARGS, jlong pagePtr) {
    return FPDFPage_GetAnnotCount((FPDF_PAGE)pagePtr);
}

JNI_FUNC(jlong, PdfiumCore, nativeOpenAnnot)(JNI_ARGS, jlong pagePtr, jint index) {
    return (jlong)FPDFPage_GetAnnot((FPDF_PAGE)pagePtr, index);
}

JNI_FUNC(void, PdfiumCore, nativeCloseAnnot)(JNI_ARGS, jlong annotPtr) {
    FPDFPage_CloseAnnot((FPDF_ANNOTATION)annotPtr);
}

JNI_FUNC(jint, PdfiumCore, nativeCountAttachmentPoints)(JNI_ARGS, jlong annotPtr) {
    return FPDFAnnot_CountAttachmentPoints((FPDF_ANNOTATION)annotPtr);
}

JNI_FUNC(jboolean, PdfiumCore, nativeGetAttachmentPoints)(JNI_ARGS, jlong pagePtr, jlong annotPtr, jlong idx, jobject p1, jobject p2, jobject p3, jobject p4) {
	jclass point = env->FindClass("android/graphics/PointF");
    jmethodID point_set = env->GetMethodID(point,"set","(FF)V");

    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);
	FS_QUADPOINTSF points={0};

	if(FPDFAnnot_GetAttachmentPoints((FPDF_ANNOTATION)annotPtr, idx, &points)) {
        double userWidth, userHeight; int deviceX, deviceY;
        int width = points.x1, height = points.y1;
        FPDF_DeviceToPage(page, 0, 0, width, height, 0, width, 0, &userWidth, &userHeight);
        FPDF_PageToDevice(page, 0, 0, userWidth, userHeight, 0, points.x1, points.y1, &deviceX, &deviceY);
        env->CallVoidMethod(p1, point_set, (float)deviceX, (float)deviceY);

        width = points.x2, height = points.y2;
        FPDF_DeviceToPage(page, 0, 0, width, height, 0, width, 0, &userWidth, &userHeight);
        FPDF_PageToDevice(page, 0, 0, userWidth, userHeight, 0, width, height, &deviceX, &deviceY);
        env->CallVoidMethod(p2, point_set, (float)deviceX, (float)deviceY);

        width = points.x3, height = points.y3;
        FPDF_DeviceToPage(page, 0, 0, width, height, 0, width, 0, &userWidth, &userHeight);
        FPDF_PageToDevice(page, 0, 0, userWidth, userHeight, 0, width, height, &deviceX, &deviceY);
        env->CallVoidMethod(p3, point_set, (float)deviceX, (float)deviceY);

        width = points.x4, height = points.y4;
        FPDF_DeviceToPage(page, 0, 0, width, height, 0, width, 0, &userWidth, &userHeight);
        FPDF_PageToDevice(page, 0, 0, userWidth, userHeight, 0, width, height, &deviceX, &deviceY);
        env->CallVoidMethod(p4, point_set, (float)deviceX, (float)deviceY);

        //env->CallVoidMethod(p2, point_set, points.x2, points.y2);
        //env->CallVoidMethod(p3, point_set, points.x3, points.y3);
        //env->CallVoidMethod(p4, point_set, points.x4, points.y4);
        return true;
	}
    return false;
}

JNI_FUNC(jlong, PdfiumCore, nativeCreateAnnot)(JNI_ARGS, jlong pagePtr, jint type) {
    auto ret = FPDFPage_CreateAnnot((FPDF_PAGE)pagePtr, type);
    FPDFAnnot_SetFlags(ret, 4);
    return (jlong)ret;
}

JNI_FUNC(jboolean, PdfiumCore, nativeSetAnnotRect)(JNI_ARGS, jlong pagePtr, jlong annotPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jdouble width, jdouble height) {
    double px, py, px1, py1;
    FPDF_DeviceToPage((FPDF_PAGE)pagePtr, 0, 0, width, height, 0, left, top, &px, &py);

    FPDF_DeviceToPage((FPDF_PAGE)pagePtr, 0, 0, width, height, 0, right, bottom, &px1, &py1);

	FS_RECTF rect{(float)px, (float)py, (float)px1, (float)py1};
    return (jboolean)FPDFAnnot_SetRect((FPDF_ANNOTATION)annotPtr, &rect);
}

JNI_FUNC(jboolean, PdfiumCore, nativeAppendAnnotPoints)(JNI_ARGS, jlong pagePtr, jlong annotPtr, jdouble left, jdouble top, jdouble right, jdouble bottom, jdouble width, jdouble height) {
    double px, py, px1, py1;
    FPDF_DeviceToPage((FPDF_PAGE)pagePtr, 0, 0, width, height, 0, left, top, &px, &py);

    FPDF_DeviceToPage((FPDF_PAGE)pagePtr, 0, 0, width, height, 0, right, bottom, &px1, &py1);

    float x=(float)px, y=(float)py, x1=(float)px1, y1=(float)py1;

	FS_QUADPOINTSF  quadpoints{x,y,x1,y,x,y1,x1,y1};

    return (jboolean)FPDFAnnot_AppendAttachmentPoints((FPDF_ANNOTATION)annotPtr, &quadpoints);
}

JNI_FUNC(jobject, PdfiumCore, nativeGetAnnotRect)(JNI_ARGS, jlong pagePtr, jint index, jint width, jint height) {
    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);
    FPDF_ANNOTATION aI = FPDFPage_GetAnnot(page, index);
    FS_RECTF rect={0};
    FPDFAnnot_GetRect(aI, &rect);
    int deviceX, deviceY;
    FPDF_PageToDevice(page, 0, 0, width, height, 0, rect.left, rect.top, &deviceX, &deviceY);

    width = rect.right-rect.left;
    height = rect.top-rect.bottom;
    rect.left = deviceX;
    rect.top = deviceY;
    rect.right = rect.left+width;
    rect.bottom = rect.top+height;

    jclass clazz = env->FindClass("android/graphics/RectF");
    jmethodID constructorID = env->GetMethodID(clazz, "<init>", "(FFFF)V");
	FPDFPage_CloseAnnot(aI);
    return env->NewObject(clazz, constructorID, rect.left, rect.top, rect.right, rect.bottom);
}

JNI_FUNC(void, PdfiumCore, nativeSetAnnotColor)(JNI_ARGS, jlong annotPtr, jint R, jint G, jint B, jint A) {
    FPDFAnnot_SetColor((FPDF_ANNOTATION)annotPtr, FPDFANNOT_COLORTYPE_Color, R, G, B, A);
}

JNI_FUNC(jlong, PdfiumCore, nativeGetAnnot)(JNI_ARGS, jlong pagePtr, jint index) {
    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);
    return (jlong)FPDFPage_GetAnnot(page, index);
}

JNI_FUNC(jlongArray, PdfiumCore, nativeGetPageLinks)(JNI_ARGS, jlong pagePtr) {
    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);
    int pos = 0;
    std::vector<jlong> links;
    FPDF_LINK link;
    while (FPDFLink_Enumerate(page, &pos, &link)) {
        links.push_back(reinterpret_cast<jlong>(link));
    }

    jlongArray result = env->NewLongArray(links.size());
    env->SetLongArrayRegion(result, 0, links.size(), &links[0]);
    return result;
}

JNI_FUNC(jlong, PdfiumCore, nativeGetLinkAtCoord)(JNI_ARGS, jlong pagePtr, jdouble width, jdouble height, jdouble posX, jdouble posY){
    double px, py;
    FPDF_DeviceToPage((FPDF_PAGE)pagePtr, 0, 0, width, height, 0, posX, posY, &px, &py);
    return (jlong)FPDFLink_GetLinkAtPoint((FPDF_PAGE)pagePtr, px, py);
}

JNI_FUNC(jstring, PdfiumCore, nativeGetLinkTarget)(JNI_ARGS, jlong docPtr, jlong linkPtr){
    DocumentFile *doc = reinterpret_cast<DocumentFile*>(docPtr);
    FPDF_LINK link = reinterpret_cast<FPDF_LINK>(linkPtr);
    FPDF_DEST dest = FPDFLink_GetDest(doc->pdfDocument, link);
    if (dest != NULL) {
        int pageIdx = FPDFDest_GetDestPageIndex(doc->pdfDocument, dest);
        char buffer[16]={0};
        buffer[0]='@';
        sprintf(buffer+1,"%d",pageIdx);
        return env->NewStringUTF(buffer);
    }
    FPDF_ACTION action = FPDFLink_GetAction(link);
    if (action == NULL) {
        return NULL;
    }
    size_t bufferLen = FPDFAction_GetURIPath(doc->pdfDocument, action, NULL, 0);
    if (bufferLen <= 0) {
        return NULL;
    }
    std::string uri;
    FPDFAction_GetURIPath(doc->pdfDocument, action, WriteInto(&uri, bufferLen), bufferLen);
    return env->NewStringUTF(uri.c_str());
}

JNI_FUNC(jobject, PdfiumCore, nativeGetLinkRect)(JNI_ARGS, jlong linkPtr) {
    FPDF_LINK link = reinterpret_cast<FPDF_LINK>(linkPtr);
    FS_RECTF fsRectF;
    FPDF_BOOL result = FPDFLink_GetAnnotRect(link, &fsRectF);

    if (!result) {
        return NULL;
    }

    jclass clazz = env->FindClass("android/graphics/RectF");
    jmethodID constructorID = env->GetMethodID(clazz, "<init>", "(FFFF)V");
    return env->NewObject(clazz, constructorID, fsRectF.left, fsRectF.top, fsRectF.right, fsRectF.bottom);
}

// Start Save PDF
struct PdfToFdWriter : FPDF_FILEWRITE {
    int dstFd;
};

static bool writeAllBytes(const int fd, const void *buffer, const size_t byteCount) {
    char *writeBuffer = static_cast<char *>(const_cast<void *>(buffer));
    size_t remainingBytes = byteCount;
     LOGE("fatal writeAllBytes: %d %d %d", buffer, byteCount, remainingBytes);
    while (remainingBytes > 0) {
        ssize_t writtenByteCount = write(fd, writeBuffer, remainingBytes);
        if (writtenByteCount == -1) {
            if (errno == EINTR) {
                continue;
            }
            LOGE("Error writing to buffer: %d", errno);
            return false;
        }
        remainingBytes -= writtenByteCount;
        writeBuffer += writtenByteCount;
    }
    return true;
}

static int writeBlock(FPDF_FILEWRITE* owner, const void* buffer, unsigned long size) {
    LOGE("fatal writeBlock: %d %d %d", buffer, size, owner);
    const PdfToFdWriter* writer = reinterpret_cast<PdfToFdWriter*>(owner);
    const bool success = writeAllBytes(writer->dstFd, buffer, size);
    if (!success) {
        LOGE("Cannot write to file descriptor. Error:%d", errno);
        return 0;
    }
    return 1;
}

JNI_FUNC(void, PdfiumCore, nativeSaveAsCopy)(JNI_ARGS, jlong docPtr, jint fd, jboolean incremental) {
    DocumentFile* docFile = (DocumentFile*)docPtr;
    PdfToFdWriter writer;
    writer.dstFd = fd;
    //writer.dstFd = docFile->fileFd;
    writer.WriteBlock = &writeBlock;
    FPDF_BOOL success = FPDF_SaveAsCopy(docFile->pdfDocument, &writer, incremental?FPDF_INCREMENTAL:FPDF_NO_INCREMENTAL); // FPDF_INCREMENTAL FPDF_NO_INCREMENTAL
    if (!success) {
        jniThrowExceptionFmt(env, "java/io/IOException", "cannot write to fd. Error: %d", errno);
    }
}

}//extern C
