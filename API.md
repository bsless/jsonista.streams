# bsless.jsonista.streams 





## `-read-values`
``` clojure

(-read-values this mapper)
```

<sub>[source](https://github.com/bsless/tools.jcmd.jfr/blob/master/src/bsless/jsonista/streams.clj#L12-L13)</sub>
## `-write-all`
``` clojure

(-write-all this writer)
```

<sub>[source](https://github.com/bsless/tools.jcmd.jfr/blob/master/src/bsless/jsonista/streams.clj#L44-L45)</sub>
## `-write-values`
``` clojure

(-write-values this values mapper options)
```

<sub>[source](https://github.com/bsless/tools.jcmd.jfr/blob/master/src/bsless/jsonista/streams.clj#L57-L58)</sub>
## `ReadValues`
<sub>[source](https://github.com/bsless/tools.jcmd.jfr/blob/master/src/bsless/jsonista/streams.clj#L12-L13)</sub>
## `WriteAll`
<sub>[source](https://github.com/bsless/tools.jcmd.jfr/blob/master/src/bsless/jsonista/streams.clj#L44-L45)</sub>
## `WriteValues`
<sub>[source](https://github.com/bsless/tools.jcmd.jfr/blob/master/src/bsless/jsonista/streams.clj#L57-L58)</sub>
## `read-values`
``` clojure

(read-values object)
(read-values object mapper)
```


Decodes a sequence of values from a JSON as an iterator
  from anything that satisfies [[ReadValue]] protocol.
  By default, File, URL, String, Reader and InputStream are supported.
  The returned object is an Iterable, Iterator and IReduceInit.
  It can be reduced on via [[reduce]] and turned into a lazy sequence
  via [[iterator-seq]].
  To configure, pass in an ObjectMapper created with [[object-mapper]],
  see [[object-mapper]] docstring for the available options.
<br><sub>[source](https://github.com/bsless/tools.jcmd.jfr/blob/master/src/bsless/jsonista/streams.clj#L115-L127)</sub>
## `write-values`
``` clojure

(write-values to object)
(write-values to object mapper)
(write-values to object mapper options)
```


Encode values as JSON and write using the provided [[WriteValue]] instance.
  By default, File, OutputStream, DataOutput and Writer are supported.
  By default, values can be an array or an Iterable.
  To configure, pass in an ObjectMapper created with [[object-mapper]],
  see [[object-mapper]] docstring for the available options.
  Additional options:
  `:write-values-as-array` - default true, write the sequence as array
  `:root-value-separator` - default nil, string separator to interpose between root values.
<br><sub>[source](https://github.com/bsless/tools.jcmd.jfr/blob/master/src/bsless/jsonista/streams.clj#L129-L143)</sub>
