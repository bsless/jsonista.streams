# io.github.bsless/jsonista.streams

Clojure library for fast streaming encoding and decoding of JSON
alongside [jsonista](https://github.com/metosin/jsonista)

## API docs

See [API.md](./API.md)

## Dependency

### Deps

```clojure
io.github.bsless/jsonista.streaming {:mvn/version "0.0.2"}
```

### Leiningen

```clojure
[io.github.bsless/jsonista.streaming "0.0.2"]
```

## Usage

### Require

```clojure
(require '[bsless.jsonista.streams :as js])
```

### Values Stream

`read-values` returns a reducible Iterator over the JSON values and is completely lazy:

```clojure
;; Just allocate an object
(read-values (json/write-value-as-string [{:a 1} {:a 2} {:a 3}]))
```

Since the resulting object is reducible, we can reduce over it without
deserializing the entire payload:

```clojure
(into
 []
 (map :a)
 (-> [{:a 1} {:a 2} {:a 3}]
     json/write-value-as-string
     (read-values json/keyword-keys-object-mapper)))
;; => [1 2 3]
```

### Streaming writing

Similarly, we can use `write-values` to directly emit values without
creating the entire blob in memory. This can be useful for quickly
getting data out of a process instead of accumulating a large blob,
consuming heap and resources.

```clojure
(.toString
 (doto (StringWriter.)
   (write-values [1 2 3])));; => "[1,2,3]"
```

We can also change the format of the output writer:

```clojure
(.toString
 (doto (StringWriter.)
   (write-values
    [1 2 3]
    json/keyword-keys-object-mapper
    {:write-value-as-array false
     :root-value-separator "\n"})))
;; => "1\n2\n3"
```

A newline separated JSON might be preferable for some APIs and consumers.

### Putting it together

By using lazy encoding and decoding together, we can construct flows
which only ever hold one decoded object in memory:

```clojure
(let [original-data [{:a 1} {:a 2} {:a 3}]
      input (json/write-value-as-string original-data)
      stream (read-values input json/keyword-keys-object-mapper)
      flow (->Eduction (map :a) stream)
      writer (StringWriter.)]
  (write-values writer flow)
  (.toString writer))
;; => "[1,2,3]"
```

## Development

Run the project's tests:

    $ clojure -T:build test

Run the project's CI pipeline and build a JAR:

    $ clojure -T:build ci

Install it locally (requires the `ci` task be run first):

    $ clojure -T:build install
    
Generate docs:

    $ clojure -M:quickdoc
    
## Future goals

Archiving this project once it gets merged into jsonista.

## License

Copyright © 2022 Ben Sless

Distributed under the Eclipse Public License version 1.0.
