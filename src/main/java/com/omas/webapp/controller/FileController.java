package com.omas.webapp.controller;

import com.omas.webapp.entity.requests.FileDownloadRequest;
import com.omas.webapp.entity.response.MessageResponse;
import com.omas.webapp.service.FileService;
import com.omas.webapp.service.TeamMemberScoreService;
import com.omas.webapp.service.UserInfoDetails;
import com.omas.webapp.table.ImageProof;
import com.omas.webapp.table.TeamMemberId;
import com.omas.webapp.table.TeamMemberScore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileAlreadyExistsException;
import java.util.Optional;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private TeamMemberScoreService scoreService;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(
        @RequestParam("competitionId") String competitionId,
        @RequestParam("file") MultipartFile file
    ) {

        Long userId = UserInfoDetails.getDetails().getId();
        TeamMemberScore score = this.scoreService.getUsersScore(userId, competitionId);

        if (score == null) {
            return new MessageResponse("Could not find a score to associate this image with", HttpStatus.BAD_REQUEST);
        }

        try {
            // This naming may be confusing but the idea is that here the server is downloading the image uploaded by the user
            this.fileService.receiveAndWriteFileFully(score.getTeamMemberId(), file);
        } catch (FileAlreadyExistsException ex) {
            return new MessageResponse("Could not upload an image with that file name: The file already exists.", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new MessageResponse("Something went wrong with uploading the image: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new MessageResponse("File uploaded successfully", HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestBody FileDownloadRequest request) {

        final TeamMemberId id = request.getTeamMemberId();
        final String fileName = request.getFileName();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Return all files associated with the TeamMemberId if no file name is provided
        if (fileName == null) {

            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();

            for (ImageProof proof : this.fileService.getFiles(id)) {
                form.add(proof.getFileName(), proof.getImageResource());
            }

            return new ResponseEntity<>(form, headers, HttpStatus.OK);

            // else statement for clarity
        } else {

            Optional<ImageProof> file = this.fileService.getFile(id, fileName);

            if (file.isEmpty()) {
                return new MessageResponse("The requested file could not be found", HttpStatus.BAD_REQUEST);
            }

            ImageProof proof = file.get();

            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();

            form.add(proof.getFileName(), proof.getImageResource());

            return new ResponseEntity<>(form, headers, HttpStatus.OK);
        }

    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("files")
    public ResponseEntity<?> getFileNames(@RequestBody TeamMemberId teamMemberId) {
        return new ResponseEntity<>(this.fileService.getFileNames(teamMemberId), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String handleValidationExceptions(HttpRequestMethodNotSupportedException ex) {
        ex.printStackTrace();
        return ex.getMessage();
    }

}
