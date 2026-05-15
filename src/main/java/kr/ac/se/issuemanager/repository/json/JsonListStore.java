package kr.ac.se.issuemanager.repository.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.se.issuemanager.repository.RepositoryException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class JsonListStore<T> {
    private final ObjectMapper mapper = JsonSupport.mapper();
    private final Path file;
    private final JavaType listType;

    JsonListStore(Path file, Class<T> itemType) {
        this.file = file;
        this.listType = mapper.getTypeFactory().constructCollectionType(List.class, itemType);
    }

    List<T> load() {
        if (Files.notExists(file)) {
            return new ArrayList<>();
        }
        try {
            if (Files.size(file) == 0) {
                return new ArrayList<>();
            }
            return new ArrayList<>(mapper.readValue(file.toFile(), listType));
        } catch (IOException exception) {
            throw new RepositoryException("JSON 파일을 읽을 수 없습니다: " + file, exception);
        }
    }

    void save(List<T> items) {
        try {
            Files.createDirectories(file.getParent());
            mapper.writeValue(file.toFile(), items);
        } catch (IOException exception) {
            throw new RepositoryException("JSON 파일에 저장할 수 없습니다: " + file, exception);
        }
    }
}

