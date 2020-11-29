#ifndef _BBW_
#define _BBW_
#include <fpdf_save.h>

class DocumentFile {
    public:
    int fileFd;
    FPDF_DOCUMENT pdfDocument = NULL;
    size_t fileSize;
    char* readBuf = NULL;
    bool responsibleForReadBuf = false;

    long readBufSt=0;
    long readBufEd=0;

    DocumentFile();
    ~DocumentFile();
};

#ifdef __cplusplus
extern "C" {
#endif


struct PdfToFdWriter : FPDF_FILEWRITE {
    int dstFd;
};

void flushBuffer(int fd);

bool writeAllBytes(const int fd, const void *buffer, const size_t byteCount);

int writeBlock(FPDF_FILEWRITE* owner, const void* buffer, unsigned long size);

int writeBlockBuffered(FPDF_FILEWRITE* owner, const void* buffer, unsigned long size);

void startBufferedWriting(DocumentFile* doc, size_t buffer_size);

#ifdef __cplusplus
}
#endif

#endif