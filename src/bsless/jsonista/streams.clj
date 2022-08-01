(ns bsless.jsonista.streams
  (:require
   [jsonista.core :as json])
  (:import
   (java.io File Reader InputStream OutputStream DataOutput Writer)
   (java.net URL)
   (java.util Iterator)
   (com.fasterxml.jackson.databind ObjectMapper SequenceWriter SerializationFeature)))

(set! *warn-on-reflection* true)

(defprotocol ReadValues
  (-read-values [this mapper]))

(extend-protocol ReadValues

  (Class/forName "[B")
  (-read-values [this ^ObjectMapper mapper]
    (.readValues (.readerFor mapper ^Class Object) ^bytes this))

  nil
  (-read-values [_ _])

  File
  (-read-values [this ^ObjectMapper mapper]
    (.readValues (.readerFor mapper ^Class Object) this))

  URL
  (-read-values [this ^ObjectMapper mapper]
    (.readValues (.readerFor mapper ^Class Object) this))

  String
  (-read-values [this ^ObjectMapper mapper]
    (.readValues (.readerFor mapper ^Class Object) this))

  Reader
  (-read-values [this ^ObjectMapper mapper]
    (.readValues (.readerFor mapper ^Class Object) this))

  InputStream
  (-read-values [this ^ObjectMapper mapper]
    (.readValues (.readerFor mapper ^Class Object) this)))

(defprotocol WriteAll
  (-write-all [this ^SequenceWriter writer]))

(extend-protocol WriteAll

  (Class/forName "[Ljava.lang.Object;")
  (-write-all [this ^SequenceWriter w]
    (.writeAll w ^"[Ljava.lang.Object;" this))

  Iterable
  (-write-all [this ^SequenceWriter w]
    (.writeAll w this)))

(defprotocol WriteValues
  (-write-values [this values mapper options]))

(defmacro ^:private -write-values*
  [this value mapper options]
  `(let [this# ~this
         options# ~options
         as-array# (:write-values-as-array options# true)
         ^String root-sep# (:root-value-separator options#)
         writer# (-> ~mapper
                     (.writerFor Object)
                     (.without SerializationFeature/FLUSH_AFTER_WRITE_VALUE))
         writer# (if root-sep#
                   (.withRootValueSeparator writer# root-sep#)
                   writer#)
         writer# (if as-array#
                   (.writeValuesAsArray writer# this#)
                   (.writeValues writer# this#))]
    (doto ^SequenceWriter (-write-all ~value writer#) (.close))))

(extend-protocol WriteValues
  File
  (-write-values [this value ^ObjectMapper mapper options]
    (-write-values* this value mapper options))

  OutputStream
  (-write-values [this value ^ObjectMapper mapper options]
    (-write-values* this value mapper options))

  DataOutput
  (-write-values [this value ^ObjectMapper mapper options]
    (-write-values* this value mapper options))

  Writer
  (-write-values [this value ^ObjectMapper mapper options]
    (-write-values* this value mapper options)))

(defn- wrap-values
  [^Iterator iterator]
  (when iterator
    (reify
      Iterable
      (iterator [this] iterator)
      Iterator
      (hasNext [this] (.hasNext iterator))
      (next [this] (.next iterator))
      (remove [this] (.remove iterator))
      clojure.lang.IReduceInit
      (reduce [_ f val]
        (loop [ret val]
          (if (.hasNext iterator)
            (let [ret (f ret (.next iterator))]
              (if (reduced? ret)
                @ret
                (recur ret)))
            ret)))
      clojure.lang.Sequential)))

(defn read-values
  "Decodes a sequence of values from a JSON as an iterator
  from anything that satisfies [[ReadValue]] protocol.
  By default, File, URL, String, Reader and InputStream are supported.
  The returned object is an Iterable, Iterator and IReduceInit.
  It can be reduced on via [[reduce]] and turned into a lazy sequence
  via [[iterator-seq]].
  To configure, pass in an ObjectMapper created with [[object-mapper]],
  see [[object-mapper]] docstring for the available options."
  ([object]
   (wrap-values (-read-values object json/default-object-mapper)))
  ([object ^ObjectMapper mapper]
   (wrap-values (-read-values object mapper))))

(defn write-values
  "Encode values as JSON and write using the provided [[WriteValue]] instance.
  By default, File, OutputStream, DataOutput and Writer are supported.
  By default, values can be an array or an Iterable.
  To configure, pass in an ObjectMapper created with [[object-mapper]],
  see [[object-mapper]] docstring for the available options.
  Additional options:
  `:write-values-as-array` - default true, write the sequence as array
  `:root-value-separator` - default nil, string separator to interpose between root values."
  ([to object]
   (write-values to object json/default-object-mapper))
  ([to object ^ObjectMapper mapper]
   (write-values to object mapper {}))
  ([to object ^ObjectMapper mapper options]
   (-write-values to object mapper options)))
