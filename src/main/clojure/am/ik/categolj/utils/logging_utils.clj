(ns am.ik.categolj.utils.logging-utils)

(defn install-slfj-bridge-handler []
  (let [^java.util.logging.Logger root-logger
        (.getLogger (java.util.logging.LogManager/getLogManager) "")]
    (doseq [handler (.getHandlers root-logger)]
      (.removeHandler root-logger handler))
    (org.slf4j.bridge.SLF4JBridgeHandler/install)))
