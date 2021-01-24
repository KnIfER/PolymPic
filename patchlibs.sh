cd LibPdfium/src/main/libs/armeabi-v7a
#patchelf --replace-needed pdfium libpdfium.so libpdfium-lib.so
#patchelf --set-soname libpdfium.so.1 libpdfium.1.so
#patchelf --set-soname libpdfium.1.so libpdfium.so

mv libpdfium.so libpdfium.1.so
#patchelf --set-soname libpdfium.so.1 libpdfium.1.so

read -n1 -p "Press any key to continue..."
