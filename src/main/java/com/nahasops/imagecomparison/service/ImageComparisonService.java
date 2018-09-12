package com.nahasops.imagecomparison.service;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface ImageComparisonService {

	public CompletableFuture<Void> compareAndCopy(final Path srcPath, final String comparisonPath,
			final File dstFolder);
	
	public CompletableFuture<Void> compare(final Path srcPath, final String comparisonPath);

}