import com.github.diszexuf.server.Server;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    @SuppressWarnings("unchecked")
    private Set<String> loadBannedWords(Path file) throws Exception {
        Method method = Server.class.getDeclaredMethod("loadBannedWords", Path.class);
        method.setAccessible(true);
        return (Set<String>) method.invoke(null, file);
    }

    @TempDir
    Path tempDir;

    @Test
    void loadBannedWords_normalFile_returnsAllWords() throws Exception {
        Path file = tempDir.resolve("banned.txt");
        Files.writeString(file, "spam\nbadword\ndrugs\n");

        Set<String> words = loadBannedWords(file);

        assertEquals(3, words.size());
        assertTrue(words.contains("spam"));
        assertTrue(words.contains("badword"));
        assertTrue(words.contains("drugs"));
    }

    @Test
    void loadBannedWords_fileWithEmptyLines_skipsEmpty() throws Exception {
        Path file = tempDir.resolve("banned.txt");
        Files.writeString(file, "spam\n\n  \nbadword\n");

        Set<String> words = loadBannedWords(file);

        assertEquals(2, words.size());
        assertFalse(words.contains(""));
        assertFalse(words.contains("  "));
    }

    @Test
    void loadBannedWords_fileWithWhitespace_trimmed() throws Exception {
        Path file = tempDir.resolve("banned.txt");
        Files.writeString(file, "  spam  \n  badword\n");

        Set<String> words = loadBannedWords(file);

        assertTrue(words.contains("spam"));
        assertTrue(words.contains("badword"));
        assertFalse(words.contains("  spam  "));
    }

    @Test
    void loadBannedWords_fileNotFound_returnsEmptySet() throws Exception {
        Path missing = tempDir.resolve("nonexistent.txt");

        Set<String> words = loadBannedWords(missing);

        assertNotNull(words);
        assertTrue(words.isEmpty());
    }

    @Test
    void loadBannedWords_emptyFile_returnsEmptySet() throws Exception {
        Path file = tempDir.resolve("empty.txt");
        Files.writeString(file, "");

        Set<String> words = loadBannedWords(file);

        assertNotNull(words);
        assertTrue(words.isEmpty());
    }

    @Test
    void loadBannedWords_resultIsUnmodifiable() throws Exception {
        Path file = tempDir.resolve("banned.txt");
        Files.writeString(file, "spam\n");

        Set<String> words = loadBannedWords(file);

        assertThrows(UnsupportedOperationException.class, () -> words.add("hacked"));
    }
}