import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JavadocGen {

    public static final String mavenRepo = "https://dl.bintray.com/first-tech-challenge/ftcsdk/";
    public static final String targetVersion = "6.0.1";

    public String[] artifacts = new String[] {
            "org.firstinspires.ftc:RobotCore",
            "org.firstinspires.ftc:FtcCommon",
            "org.firstinspires.ftc:Hardware",
            "org.firstinspires.ftc:OnBotJava",
            "org.firstinspires.ftc:Inspection",
            "org.firstinspires.ftc:Blocks",
    };

    public File createDestDir(String artifactId, String targetVersion) {
        File destination = new File("docs/" + targetVersion + "/" +artifactId + "/");
        try {
            FileUtils.deleteDirectory(destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
        destination.mkdirs();
        return destination;
    }

    public void fetch(String artifact) {
        Scanner sc = new Scanner(artifact);
        sc.useDelimiter(":");

        String groupId = sc.next();
        String artifactId = sc.next();
        String version = targetVersion;

        File destination = createDestDir(artifactId, targetVersion);

        try {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(mavenRepo)
                    .append(groupId.replaceAll("\\.", "/")).append("/")
                    .append(artifactId).append("/")
                    .append(version).append("/")
                    .append(artifactId).append("-").append(version)
                    .append("-javadoc.jar");

            URL url = new URL(urlBuilder.toString());
            ZipInputStream zin = new ZipInputStream(url.openStream());
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                try {
                    File f = new File(destination, ze.getName());

                    if (ze.isDirectory()) {
                        f.mkdirs();
                        zin.closeEntry();
                        continue;
                    }

                    OutputStream baos = new FileOutputStream(f);
                    ReadableByteChannel in = Channels.newChannel(zin);
                    WritableByteChannel out = Channels.newChannel(baos);
                    ByteBuffer buffer = ByteBuffer.allocate(65536);
                    while (in.read(buffer) != -1) {
                        buffer.flip();
                        out.write(buffer);
                        buffer.clear();
                    }

                    zin.closeEntry();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void process() {
        for (String artifact : artifacts) {
            System.out.println("Processing " + artifact);
            fetch(artifact);
        }
    }

    public static void main(String... args) throws Exception {
        JavadocGen gen = new JavadocGen();
        gen.process();
        System.out.println("Done");
    }
}
