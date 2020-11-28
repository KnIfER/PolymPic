#include "util.hpp"

#include <unistd.h>
#include <string.h>
#include <utils/Errors.h>

#include <fpdfview.h>
#include <fpdf_save.h>

#include "BufferedBlockWriter.h"

using namespace android;

bool writeAllBytes(const int fd, const void *buffer, const size_t byteCount) {
    char *writeBuffer = static_cast<char *>(const_cast<void *>(buffer));
    size_t remainingBytes = byteCount;
    LOGE("fatal writeAllBytes: %ld %ld", (long)buffer, (long)byteCount);
    while (remainingBytes > 0) {
        ssize_t writtenByteCount = write(fd, writeBuffer, remainingBytes);
        if (writtenByteCount == -1) {
            if (errno == EINTR) {
                continue;
            }
            LOGE("fatal Error writing to buffer: %d", errno);
            return true;
        }
        remainingBytes -= writtenByteCount;
        writeBuffer += writtenByteCount;
    }
    return true;
}

int writeBlock(FPDF_FILEWRITE* owner, const void* buffer, unsigned long size) {
    LOGE("fatal writeBlock: %ld %ld", (long)buffer, size);
    const PdfToFdWriter* writer = reinterpret_cast<PdfToFdWriter*>(owner);
    const bool success = writeAllBytes(writer->dstFd, buffer, size);
    if (!success) {
        LOGE("fatal Cannot write to file descriptor. Error:%d", errno);
        return 0;
    }
    return 1;
}

static size_t buf_length = 1024*1024*1;

static char* buf = new char[buf_length];

static size_t count;

int error = 0;

void flushBuffer(int fd) {
    //LOGE("fatal flushBuffer count=%ld  buf_length=%ld", count, buf_length);
    if (count > 0) {
        if(!writeAllBytes(fd, buf, count)) {
            error = 1;
        }
        count = 0;
    }
}

bool writeAllBytesBuffered(const int fd, const void *buffer, const size_t len) {
    if (len >= buf_length) {
        /* If the request length exceeds the size of the output buffer,
        flush the output buffer and then write the data directly.
        In this way buffered streams will cascade harmlessly. */
        flushBuffer(fd);
        return writeAllBytes(fd, buffer, len);
    }
    if (len > buf_length - count) {
        flushBuffer(fd);
    }
    if(error) {
        return false;
    }
    memcpy(buf+count, buffer, len);
    count += len;
    return true;
}

int writeBlockBuffered(FPDF_FILEWRITE* owner, const void* buffer, unsigned long size) {
    LOGE("fatal writeBlock: %ld %ld", (long)buffer, size);
    const PdfToFdWriter* writer = reinterpret_cast<PdfToFdWriter*>(owner);
    const bool success = writeAllBytesBuffered(writer->dstFd, buffer, size);
    if (!success) {
        LOGE("fatal Cannot write to file descriptor. Error:%d", errno);
        return 0;
    }
    //flushBuffer(writer->dstFd);
    return 1;
}


void startBufferedWriting(size_t buffer_size) {
    count = 0;
}
