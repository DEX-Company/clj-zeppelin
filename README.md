# clj-zeppelin

A Clojure library for managing [Zeppelin](https://zeppelin.apache.org) notebooks using the Zeppelin [Notebook REST API](https://zeppelin.apache.org/docs/0.8.0/usage/rest_api/notebook.html).



 The operations defined in this library are as follows :     
                                             

*   [**create-note!**](
#create-note)
*   [**list-notes**](
#list-notes)

*   [**delete-note!**](
#delete)
*   [**create-paragraph!**](
#create-para)
*   [**run-paragraph-async**](
#run-para-async)
*   [**run-paragraph-sync**](
#run-para-sync)
*   [**get-paragraph-status**](
#get-para-sts)
*   [**get-paragraph-info**](
#get-para-info)
*   [**run-all-paragraphs**](
#run-para)


***



* ### <a name= "create-note" ></a> create-note!  
_This POST method creates a new note and returns the id of the newly created note._

* Function call :<br> <pre>
               (create-note! notebook-server-url payload) 
</pre>

* Input fields : <br><pre>
              **notebook-server-url** - Zeppelin server url <br>
              **payload** - name of the new note to be created  without paragraph - {:name "Ocean Notebook"}                          
&nbsp;&nbsp; &nbsp; &nbsp; &nbsp;- name of the new note to be created  with paragraph - {:name "Ocean Notebook"} :paragraphs [{:title "intro" :text "Hello world"}]}  
</pre>

* Example :<br><pre>
         (create-note! "http://[zeppelin-server]:[zeppelin-port]" {:name "Ocean Notebook"}) 
</pre>

* Output :<br><pre>
        2DTW93XQ9
</pre>

***
* ### <a name= "list-notes" ></a>  list-notes  <br>

_This GET method returns a JSON array containing the name and id of all the available notes in the zeppelin server._

* Function call : <br><pre>
               (list-notes notebook-server-url)
</pre>

* Input fields : <br><pre>
               **notebook-server-url** - Zeppelin server url </pre>

* Example :<br><pre>
         (list-notes "http://[zeppelin-server]:[zeppelin-port]") 
</pre>

* Output : <pre>
        {:status "OK", :message "", :body  [{:name "About this Build", :id "2BTRWA9EV"} {:name    "Zeppelin Tutorial/Basic 
        Features (Spark)", :id "2A94M5J1Z"} {:name  "Zeppelin Tutorial/Matplotlib (Python • PySpark)", :id "2C2AUG798"} 
        {:name  "Zeppelin Tutorial/R  (SparkR)", :id "2BWJFTXKJ"} {:name  "Zeppelin Tutorial/Using Flink for batch 
        processing",:id "2C35YU814"} {:name  "ZeppelinTutorial/Using Mahout", :id "2BYEZ5EVK"} {:name  "Zeppelin 
        Tutorial/Using Pig for querying data",:id "2C57UKYWR"}]}
</pre>


***

* ### <a name= "delete" ></a>  delete-note!  <br/>
_This DELETE method deletes a note on the zeppelin server by the given note id and returns a JSON array containing status._
* Function call : <br><pre>
               (delete-note! notebook-server-url note-id)
</pre>

* Input fields : <br><pre>
              **notebook-server-url** - Zeppelin server url <br>
              **note-id** - id of the notebook to be deleted
</pre>

* Example :<br><pre>
         (delete-note! "http://[zeppelin-server]:[zeppelin-port]" "2DTW93XQ9") 
</pre>

* Output : <br><pre>
        {:status "OK", :message ""}
</pre>

***

* ### <a name= "create-para" ></a> create-paragraph! 

_This POST method adds a new paragraph to an existing note and returns id of the newly created paragraph._

* Function call : <br><pre>
               (create-paragraph! notebook-server-url note-id paragraph_data)
</pre>

* Input fields :<br><pre>
               **notebook-server-url** - Zeppelin server url  
               **note-id** - id of existing notebook in zeppelin <br>
               **paragraph_data** - content to be added to the paragraph 
</pre>

* Example :<br><pre>
         (create-paragraph! "http://[zeppelin-server]:[zeppelin-port]" "2DTW93XQ9" (-> {:title "intro" :text (10 + 10)}))                                                    
</pre>

* Output : <br><pre>
        20181029-070332_2137251371
</pre>

***

* ### <a name= "run-para-async" ></a> run-paragraph-async 

_This POST method runs the paragraph asynchronously by given note and paragraph id and returns an OK message._

* Function call :<br><pre>
               (run-paragraph-async notebook-server-url note-id paragraph-id)
</pre>

* Input fields :<br><pre>
              **notebook-server-url** - Zeppelin server url  
              **note-id** - id of the existing notebook in zeppelin  
              **paragraph-id** -  id of the paragraph in the notebook 
</pre>

* Example :<br><pre>
         (run-paragraph-async "http://[zeppelin-server]:[zeppelin-port]" "2DTW93XQ9" "20181029-070332_2137251371")                                                    
</pre>

* Output : <br><pre>
         OK
</pre>

***

* ### <a name= "run-para-sync" ></a>  run-paragraph-sync  
 
_This POST method runs the paragraph synchronously by given note and paragraph id and returns a JSON array containing SUCCESS or ERROR status, depending on the outcome of paragraph execution._

* Function call :<br><pre>
               (run-paragraph-sync notebook-server-url note-id paragraph-id)
</pre>

* Input fields : <br><pre>
              **notebook-server-url** - Zeppelin server url  
              **note-id** - id of the existing notebook in zeppelin  
              **paragraph-id** -  id of the paragraph in the notebook 
</pre>

* Example :<br><pre>
         (run-paragraph-sync "http://[zeppelin-server]:[zeppelin-port]" "2DTW93XQ9" "20181029-070332_2137251371")                                                    
</pre>

* Output :<br><pre>
        {:code "SUCCESS", :msg  [{:type "TEXT", :data "\nres1: Int = 20\n"}]}
</pre>


***

* ### <a name= "get-para-sts" ></a> get-paragraph-status 

_This GET method gets the status of a single paragraph by the given note and paragraph id. The returned JSON array contains  the paragraph id, paragraph status, paragraph finish date, paragraph start date._

* Function call :<br><pre>
               (get-paragraph-status notebook-server-url note-id paragraph-id)
</pre>

* Input fields : <br><pre>
               **notebook-server-url** - Zeppelin server url  
               **note-id** - id of the existing notebook in zeppelin  
               **paragraph-id** -  id of the paragraph in the notebook 
</pre>

* Example :<br><pre>
         (get-paragraph-status "http://[zeppelin-server]:[zeppelin-port]" "2DTW93XQ9" "20181029-070332_2137251371")                                                    
</pre>

* Output : <br><pre>
        {:progress "100", :started  "Mon Oct 29 07:20:06 UTC 2018", :finished  "Mon Oct 29 07:20:07 UTC 2018",
         :id "20181029-070332_2137251371", :status "FINISHED"}
</pre>

***


* ### <a name= "get-para-info" ></a>  get-paragraph-info <br/>
_This GET method retrieves an existing paragraph's information using the given id. The returned JSON array contains information about the paragraph._

* Function call :<br><pre>
               (get-paragraph-info notebook-server-url note-id paragraph-id)
</pre>

* Input fields :<br><pre>
              **notebook-server-url** - Zeppelin server url    
              **note-id** - id of the existing notebook in zeppelin  
              **paragraph-id** -  id of the paragraph in the notebook 
</pre>

* Example :<br><pre>
         (get-paragraph-info "http://[zeppelin-server]:[zeppelin-port]" "2DTW93XQ9" "20181029-070332_2137251371")                                                    
</pre>

* Output :<br><pre>
        {:apps [], :dateFinished  "Oct 29, 2018 7:20:07 AM", :jobName  "paragraph_1540796612560_-766558253", :config {},
         :dateUpdated  "Oct 29, 2018 7:03:32 AM", :settings {:params {}, :forms {}}, :title "intro", :status "FINISHED",
         :id "20181029-070332_2137251371", :progressUpdateIntervalMs 500, :dateCreated  "Oct 29, 2018 7:03:32 AM",
         :dateStarted  "Oct 29, 2018 7:20:06 AM", :user "anonymous", :text "20", :results  {:code "SUCCESS",  :msg
         [{:type "TEXT",    :data "\nres1: Int = 20\n"}]}}
</pre>


***

* ### <a name= "run-para" ></a>  run-all-paragraphs 

_This POST method runs all paragraphs in the specified note and returns a status message._

* Function call :<br><pre>
               (run-all-paragraphs notebook-server-url note-id)
</pre>

* Input fields :<br><pre>
              **notebook-server-url** - Zeppelin server url    
              **note-id** - id of the existing notebook in zeppelin  
</pre>

* Example :<br><pre>
         (run-all-paragraphs "http://[zeppelin-server]:[zeppelin-port]" "2DTW93XQ9")                                                    
</pre>

* Output :<br><pre>
        {:status "OK"}
</pre>

***
## Development

### Testing:

* Install Docker
* Verify that Zeppelin Docker instance is available locally by running `docker run -p 8080:8080 apache/zeppelin:0.8.0`
* Install [Leiningen](https://leiningen.org/) 
* Run `lein test` should create the Zeppelin docker container and run the tests

## Installation:

* To install, add the following dependency to your project or build file:
<pre>
      [clj-zeppelin "0.1.0-SNAPSHOT"]
</pre>

## License

Copyright Â© 2018 Dex

Distributed under the Apache 2.0 License
