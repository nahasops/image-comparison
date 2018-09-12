package com.nahasops.imagecomparison;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.nahasops.imagecomparison.exception.ExitException;
import com.nahasops.imagecomparison.service.ImageComparisonService;

@SpringBootApplication
public class ImageComparisonApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(ImageComparisonApplication.class);

	@Autowired
	private ImageComparisonService imageComparisonService;

	@Value("${input.path}")
	private String inputPath;

	@Value("${output.path}")
	private String outputPath;

	@Value("${output.move.to}")
	private String outputPathMoveTo;
	
	@Value("${file.extension}")
	private String fileExtension;

	public static void main(String[] args) {
		SpringApplication.run(ImageComparisonApplication.class, args);
	}

	@Override
	public void run(String... args) {

		logger.info("Starting comparison process with InputPath = {}  and  OutputPath = {}", inputPath, outputPath);

		long start = System.currentTimeMillis();

		List<CompletableFuture<?>> jobs = new ArrayList<>();

		try {

			Files.walk(Paths.get(inputPath))
					.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(fileExtension)).forEach(k -> {
						jobs.add(imageComparisonService.compareAndCopy(k, outputPath, Paths.get(outputPathMoveTo).toFile()));
					});

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		// Wait until they are all done
		CompletableFuture.allOf(jobs.toArray(new CompletableFuture[jobs.size()])).join();
		
		logger.info("Elapsed time for whole process : {} ", (System.currentTimeMillis() - start));

		if (args.length > 0 && args[0].equals("exitcode")) {
			throw new ExitException();
		}

		System.exit(0);
	}

	public String getInputPath() {
		return this.inputPath;
	}

	public String getOutputPath() {
		return this.outputPath;
	}

	public String getOutputPathMoveTo() {
		return this.outputPathMoveTo;
	}

	public String getFileExtension() {
		return fileExtension;
	}

}
