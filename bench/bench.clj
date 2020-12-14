#!/usr/bin/env clojure -M

"USAGE: ./bench [rebuild]? [<version>|<version-vm> ...]? [<bench-name> ...]?"

(require
  '[clojure.edn :as edn]
  '[clojure.java.io :as io]
  '[clojure.java.shell :as sh]
  '[clojure.string :as str])

(defn sh [& cmd]
  (let [res (apply sh/sh cmd)]
    (when (not= 0 (:exit res))
      (throw (ex-info "ERROR" res)))
    (str/trim (:out res))))

(defn copy [^java.io.InputStream input ^java.io.Writer output]
  (let [^"[C" buffer (make-array Character/TYPE 1024)
        in (java.io.InputStreamReader. input "UTF-8")
        w  (java.io.StringWriter.)]
    (loop []
      (let [size (.read in buffer 0 (alength buffer))]
        (if (pos? size)
          (do (.write output buffer 0 size)
              (.flush output)
              (.write w buffer 0 size)
              (recur))
          (str w))))))

(defn run [& cmd]
  (let [cmd  (remove nil? cmd)
        proc (.exec (Runtime/getRuntime)
                    (into-array String cmd)
                    (@#'sh/as-env-strings sh/*sh-env*)
                    (io/as-file sh/*sh-dir*))
        out  (promise)]
    (with-open [stdout (.getInputStream proc)
                stderr (.getErrorStream proc)]
      (future (deliver out (copy stdout *out*)))
      (future (copy stderr *err*))
      (.close (.getOutputStream proc))
      (let [code (.waitFor proc)]
        (when (not= code 0)
          (throw (ex-info "ERROR" {:cmd cmd :code code})))
        @out))))


(def opts
  (loop [opts {:rebuild    false
               :versions   []
               :benchmarks []}
         args *command-line-args*]
    (if-some [arg (first args)]
      (cond
        (= "rebuild" arg)
        (recur (assoc opts :rebuild true) (next args))

        (re-matches #"(datalevin|datascript|datomic)" arg)
        (recur (update opts :versions conj ["latest" arg]) (next args))

        (re-matches #"(\d+\.\d+\.\d+|[0-9a-fA-F]{40}|latest)" arg)
        (recur (update opts :versions conj [arg "datalevin"]) (next args))

        (re-matches #"(\d+\.\d+\.\d+|[0-9a-fA-F]{40}|latest)-(datalevin|datascript|datomic)" arg)
        (let [[_ version vm] (re-matches #"(\d+\.\d+\.\d+|[0-9a-fA-F]{40}|latest)-(datalevin|datascript|datomic)" arg)]
          (recur (update opts :versions conj [version vm]) (next args)))

        :else
        (recur (update opts :benchmarks conj arg) (next args)))
      opts)))


(defn run-benchmarks [version vm benchmarks]
  (case vm
    "datalevin"
    (apply run "clojure" "-Sdeps"
           (cond
             (= "latest" version)
             (str "{:paths [\"src\"]"
                  "    :deps {datalevin/datalevin {:local/root \"..\"} org.lmdbjava/lmdbjava {:mvn/version \"0.8.1\"} com.taoensso/nippy {:mvn/version \"3.1.1\"} org.roaringbitmap/RoaringBitmap {:mvn/version \"0.9.3\"}}}")

             (re-matches #"\d+\.\d+\.\d+" version)
             (str "{:paths [\"src\"]"
                  "    :deps {datalevin/datalevin {:mvn/version \"" version "\"}}}")

             (re-matches #"[0-9a-fA-F]{40}" version)
             (str "{:paths [\"src\"]"
                  "    :deps {datalevin/datalevin {:git/url \"https://github.com/juji-io\" :sha \"" version "\"}}}"))
           "-M" "-m" "datalevin-bench.datalevin"
           benchmarks)

    "datascript"
    (apply run "clojure" "-Sdeps"
           (str "{"
                " :paths [\"src\"]"
                " :deps {datascript/datascript {:mvn/version \"" (if (= "latest" version) "1.0.0" version) "\"}}"
                "}")
           "-M" "-m" "datascript-bench.datascript"
           benchmarks)

    "datomic"
    (apply run "clojure" "-Sdeps"
           (str "{"
                " :paths [\"src\" \"src-datomic\"]"
                " :deps {com.datomic/datomic-free {:mvn/version \"" (if (= "latest" version) "0.9.5697" version) "\"}}"
                "}")
           "-M" "-m" "datalevin-bench.datomic"
           benchmarks)
    ))


(def default-benchmarks
  [
   ;; "add-1"
   ;; "add-5"
   ;; "add-all"
   ;; "init"
   ;; "retract-5"
   "q1"
   "q2"
   "q3"
   "q4"
   ;; "q5"
   "qpred1"
   "qpred2"])


(def default-versions
  [;["latest" "datomic"]
   ;["0.18.13" "datascript"]
   ["latest" "datascript"]
   ["latest" "datalevin"]])


(binding [sh/*sh-env* (merge {} (System/getenv) {})
          sh/*sh-dir* "."]
  (let [{:keys [rebuild benchmarks versions]} opts]
    (when rebuild
      (binding [sh/*sh-dir* ".."]
        (run "lein" "do" "clean," "javac," "cljsbuild" "once" "bench")))
    (let [benchmarks (if (empty? benchmarks) default-benchmarks benchmarks)
          versions   (if (empty? versions)   default-versions    versions)]
      (print "version   \t")
      (doseq [b benchmarks] (print b "\t"))
      (println)
      (doseq [[version vm] versions]
        (print (str version "-" vm) "\t")
        (flush)
        (run-benchmarks version vm benchmarks)))))

(shutdown-agents)
; (System/exit 0)
