/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.builder;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Compressor {

    private static final CompressorStreamFactory factory = new CompressorStreamFactory();

    private final String extension;
    private final String format;

    public Compressor(String extension, String format) {
        this.extension = extension;
        this.format = format;
    }

    public String transformPathname(String filename) {
        return filename + "." + extension;
    }

    public InputStream createInputStream(InputStream inputStream) throws IOException {
        try {
            return factory.createCompressorInputStream(format, inputStream);
        } catch (CompressorException e) {
            throw new IOException("Failed to create decompressor", e);
        }
    }

    public OutputStream createOutputStream(OutputStream outputStream) throws IOException {
        try {
            return factory.createCompressorOutputStream(format, outputStream);
        } catch (CompressorException e) {
            throw new IOException("Failed to create compressor", e);
        }
    }

}
