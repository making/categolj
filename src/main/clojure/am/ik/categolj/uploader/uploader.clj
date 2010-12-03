(ns am.ik.categolj.uploader.uploader)

(defprotocol UploadManager
  (upload [this file])
  (delete-uploaded-file-by-id [this id])
  (get-uploaded-files-by-page [this page count]))
