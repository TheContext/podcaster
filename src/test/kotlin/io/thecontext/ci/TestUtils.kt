package io.thecontext.ci


fun loadResourceFile(fileInResourceFolder: String) = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(fileInResourceFolder);
