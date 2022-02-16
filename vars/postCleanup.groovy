void call(Map args = [:]) {
    echo"I'm running!!!!!!"
    cleanWs(disableDeferredWipeout: true, deleteDirs: true)
}