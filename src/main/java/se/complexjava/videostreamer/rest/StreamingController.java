package se.complexjava.videostreamer.rest;


import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@RestController
public class StreamingController {


    @GetMapping("/videos/{name}")
    public ResponseEntity<ResourceRegion> getVideo(@PathVariable(name = "name") String name, @RequestHeader HttpHeaders headers) throws Exception{

        //Path file = Paths.get("./videos/" + name);//path syntax for running on windows
        Path file = Paths.get("/videos/" + name);//linux path syntax for accessing volume in container

        UrlResource video = new UrlResource(file.toUri());

        ResourceRegion region = resourceRegion(video, headers);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory
                        .getMediaType(video)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(region);
    }


    private ResourceRegion resourceRegion(UrlResource video, HttpHeaders headers) throws Exception{

        long contentLength = video.contentLength();
        List<HttpRange> range = headers.getRange();

        if(!range.isEmpty()){

            long start = range.get(0).getRangeStart(contentLength);//make this less hardcoded
            long end = range.get(0).getRangeEnd(contentLength);//    -//-
            long rangeLength = Long.min(1 * 1024 * 1024, end - start + 1);//sets rangeLength to max 1 mb

            return new ResourceRegion(video, start, rangeLength);

        }else{

            long rangeLength = Long.min(1 * 1024 * 1024, contentLength);

            return new ResourceRegion(video, 0, rangeLength);
        }
    }
}
