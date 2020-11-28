#ifndef _BBW_
#define _BBW_
#include <fpdf_save.h>

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

void startBufferedWriting(size_t buffer_size);

#ifdef __cplusplus
}
#endif

#endif