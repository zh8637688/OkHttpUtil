package cz.okhttputil.request;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by haozhou on 2017/6/1.
 */
public class PostFileRequest extends Request {
    Map<String, File> files;

    PostFileRequest() {
        method = Method.POST;
    }

    public Map<String, File> getFiles() {
        return files;
    }

    public static class Builder extends Request.Builder {
        Map<String, File> files;

        public Builder(String url) {
            super(url);
            files = new HashMap<>();
        }

        public Builder addFile(String name, String filePath) {
            File file = new File(filePath);
            if (file.exists()) {
                this.files.put(name, file);
            }
            return this;
        }

        public PostFileRequest build() {
            PostFileRequest request = new PostFileRequest();
            request.method = method;
            request.url = url;
            request.params = params;
            request.encodeParams = encodeParams;
            request.header = header;
            request.timeoutInSec = timeoutInSec;
            request.retryCount = retryCount;
            request.files = files;
            return request;
        }
    }
}
