(ns bsless.jsonista.streams-test
  (:require
   [clojure.test :as t]
   [jsonista.core :as j]
   [bsless.jsonista.streams :as s]
   [clojure.string :as str])
  (:import
   (java.io
    ByteArrayInputStream
    File
    FileOutputStream
    FileWriter
    InputStreamReader
    RandomAccessFile)))

(defn- str->input-stream [^String x] (ByteArrayInputStream. (.getBytes x "UTF-8")))

(defn tmp-file ^File [] (File/createTempFile "temp" ".json"))

(t/deftest read-values-iteration
  (let [original [{"ok" 1}]
        ^java.util.Iterator it (s/read-values (j/write-value-as-bytes original))]
    (t/is (instance? java.util.Iterator it))
    (t/is (.hasNext it))
    (t/is (= (first original) (.next it)))
    (t/is (false? (.hasNext it)))))

(t/deftest read-values-reduction
  (let [original [{"ok" 1}]
        ^java.util.Iterator it (s/read-values (j/write-value-as-bytes original))
        xf (map #(update % "ok" inc))]
    (t/is (= (into [] xf original) (into [] xf it)))))


(t/deftest write-values-types
  (let [original [{"ok" 1}]
        expected (j/write-value-as-string original)
        file (tmp-file)]

    (t/testing "File"
      (s/write-values file original)
      (t/is (= expected (slurp file)))
      (.delete file))

    (t/testing "OutputStream"
      (s/write-values (FileOutputStream. file) original)
      (t/is (= expected (slurp file)))
      (.delete file))

    (t/testing "DataOutput"
      (s/write-values (RandomAccessFile. file "rw") original)
      (t/is (= expected (slurp file)))
      (.delete file))

    (t/testing "Writer"
      (s/write-values (FileWriter. file) original)
      (t/is (= expected (slurp file)))
      (.delete file))))

(t/deftest read-values-types
  (let [original [{"ok" 1}]
        input-string (j/write-value-as-string original)
        file (tmp-file)]
    (spit file input-string)

    (t/testing "nil"
      (t/is (= nil (s/read-values nil))))

    (t/testing "byte-array"
      (t/is (= original (s/read-values (j/write-value-as-bytes original)))))

    (t/testing "File"
      (t/is (= original (s/read-values file))))

    (t/testing "URL"
      (t/is (= original (s/read-values (.toURL file)))))

    (t/testing "String"
      (t/is (= original (s/read-values input-string))))

    (t/testing "InputStream"
      (t/is (= original (s/read-values (str->input-stream input-string)))))

    (t/testing "Reader"
      (t/is (= original (s/read-values (InputStreamReader. (str->input-stream input-string))))))))

(t/deftest write-values-iterable
  (let [original [{"ok" 1}]
        xf (map #(update % "ok" inc))
        expected (j/write-value-as-string (into [] xf original))
        file (tmp-file)
        eduction (->Eduction xf original)]

    (s/write-values file eduction)
    (t/is (= expected (slurp file)))
    (.delete file)))

(t/deftest write-values-options
  (let [original [{"a" 1} {"b" 2}]
        file (tmp-file)]

    (t/testing "As Array"
      (t/testing "With root value separator"
        (s/write-values file original j/default-object-mapper {:write-value-as-array true
                                                               :root-value-separator "\n"})
        (t/is (= (j/write-value-as-string original) (slurp file)))
        (.delete file))
      (t/testing "Without root value separator"
        (s/write-values file original j/default-object-mapper {:write-value-as-array true})
        (t/is (= (j/write-value-as-string original) (slurp file)))
        (.delete file)))
    (t/testing "As Values"
      (t/testing "With root value separator"
        (s/write-values file original j/default-object-mapper {:write-value-as-array false
                                                               :root-value-separator "\n"})
        (t/is (= (str/join "\n" (map j/write-value-as-string original)) (slurp file)))
        (.delete file))
      (t/testing "Without root value separator"
        (s/write-values file original j/default-object-mapper {:write-value-as-array false})
        (t/is (= (str/join " " (map j/write-value-as-string original)) (slurp file)))
        (.delete file)))))
