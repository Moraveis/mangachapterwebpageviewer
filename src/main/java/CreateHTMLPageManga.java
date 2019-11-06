import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CreateHTMLPageManga {

    private static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static String MANGAFOLDER = "/Users/joao/Documents/Mangas/";
    private static String MANGANAME = "SoloLeveling";
    private static String RESOURCEFILENAME = "manga_page_viewer.html";

    public static void main(String[] args) {
        readFolder(MANGAFOLDER.concat(MANGANAME).concat("/"));
    }

    private static void readFolder(String basePath) {
        try {
            Stream<Path> paths = Files.walk(Paths.get(basePath));
            Map<String, Object> mapper = new HashMap<>();

            paths.filter(Files::isDirectory)
                    .filter(f -> !f.getFileName().toString().equals(MANGANAME))
                    .forEach(dir -> {
                        try {
                            List<String> lstImages = Files.walk(Paths.get(dir.toUri()))
                                    .filter(Files::isRegularFile)
                                    .map(file -> file.getFileName().toString())
                                    .collect(Collectors.toList());

                            if (!lstImages.isEmpty()) {
                                mapper.put("mangaName", dir.getName(4).toString());
                                mapper.put("mangaChapter", dir.getFileName().toString());
                                mapper.put("images", lstImages);
                            }
        //                    System.out.println(mapper + "\n\n");

                            generateFile(mapper);
                        } catch (Exception io) {
                            log.log(Level.SEVERE, "Failed to read folder. Path: {0} - {1}", new Object[]{basePath, io});
                        }
                    });
        } catch (IOException iox) {
            log.log(Level.SEVERE, "Failed to read folder. Path: {0}", new Object[]{basePath});
        }
    }

    private static void generateFile(Map<String, Object> mapper) {
        try {
            ClassLoader classLoader = new CreateHTMLPageManga().getClass().getClassLoader();
            File file = new File(classLoader.getResource(RESOURCEFILENAME).getFile());

            String mangaName = mapper.get("mangaName").toString();
            String mangaChapter = mapper.get("mangaChapter").toString();

            String content = new String(Files.readAllBytes(file.toPath()));
            content = content.replace("${mangaName}", mangaName);
            content = content.replace("${mangaChapter}", mangaChapter);

            StringBuffer imgBlock = new StringBuffer();
            List<String> list = (List<String>) mapper.get("images");

            list.stream().forEach(image -> {
                imgBlock.append("<p class='image'><img src='" + image + "'/></p>");
            });

            content = content.replace("${mangaImages}", imgBlock.toString());

            String filename = mapper.get("mangaChapter").toString().replace(" ", "").concat(".html");

            BufferedWriter writer = new BufferedWriter(new FileWriter(MANGAFOLDER.concat(MANGANAME).concat("/").concat(mangaChapter).concat("/").concat(filename)));
            writer.write(content);
            writer.close();
        } catch (IOException io) {
            io.printStackTrace();
        }

    }
}
