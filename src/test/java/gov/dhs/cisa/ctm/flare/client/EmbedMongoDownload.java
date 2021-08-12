package gov.dhs.cisa.ctm.flare.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbedMongoDownload {
	private static final Logger log = LoggerFactory.getLogger(EmbedMongoDownload.class);

	private static final String srcDirectoryPath = "src/test/resources/";

	// private static final String linuxDistFile = "mongodb-linux-x86_64-3.2.2.tgz";
	private static final String linuxDistFile = "mongodb-linux-x86_64-3.5.5.tgz";
	private static final String win32DistFile = "mongodb-win32-x86_64-3.2.2.zip";
	private static final String osxDistFile = "mongodb-osx-x86_64-3.2.2.tgz";

	public static void downloadDist() {
		String os = System.getProperty("os.name");
		String home = System.getProperty("user.home");

		log.debug("OS NAME: " + os);
		log.debug("USER HOME: " + home);

		Path src = Paths.get(getSrcPath(os));
		Path dest = Paths.get(getDestPath(os, home));
		try {
			if (!Files.exists(dest)) {
				Files.createDirectories(dest);
			}
			Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getDestPath(String os, String home) {
		String osName = getOS(os);
		switch (osName) {
		case ("Windows"):
			return home + "/.embedmongo/win32/" + win32DistFile;
		case ("Linux"):
			return home + "/.embedmongo/linux/" + linuxDistFile;
		case ("Mac"):
			return home + "/.embedmongo/osx/" + osxDistFile;
		default:
			return null;
		}
	}

	public static String getSrcPath(String os) {
		String osName = getOS(os);
		switch (osName) {
		case ("Windows"):
			return srcDirectoryPath + win32DistFile;
		case ("Linux"):
			return srcDirectoryPath + linuxDistFile;
		case ("Mac"):
			return srcDirectoryPath + osxDistFile;
		default:
			return null;
		}
	}

	private static String getOS(String os) {
		return os.split(" ")[0];
	}

}
