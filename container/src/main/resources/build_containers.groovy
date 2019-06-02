class BuildContainer {

    def containerRegistry = ""
    static final def ARCH = ['amd64': 'amd64', 'arm32v7': 'arm', 'arm64v8': 'arm64']
    def version
    def basedir
    def workdir
    def containerName

    BuildContainer(File basedir, String version, String containerName) {
        this.basedir = basedir
        this.workdir = new File(basedir, "target")
        this.version = version
        this.containerName = containerName
        if(System.getenv("CONTAINER_REGISTRY") != null) {
            containerRegistry = System.getenv("CONTAINER_REGISTRY") + "/"
        }
    }

    void execute(String command, int timeout = 60000) {
        def stdout = new StringBuilder()
        def stderr = new StringBuilder()
        def proc = command.execute(null, workdir)
        proc.consumeProcessOutput(stdout, stderr)
        proc.waitForOrKill(timeout)
        System.out.println(stdout)
        System.out.println(stderr)
    }

    void setupMultiarchBinaries() {
        execute("docker run --rm --privileged multiarch/qemu-user-static:register --reset")
    }

    void buildContainerImages() {
        ARCH.each { k, v -> execute("docker build --build-arg ARCH=${k} -t ${containerRegistry}${containerName}-${k}:${version} .")}
    }

    void pushContainerImages() {
        if(containerRegistry.length() > 0) {
            ARCH.each { k, v -> execute("docker push ${containerRegistry}${containerName}-${k}:${version}") }
        }
    }

    void buildContainerManifest() {
        def manifestName = "${containerRegistry}${containerName}:${version}"
        def manifests = ARCH.collect { k,v -> "${containerRegistry}${containerName}-${k}:${version}"}.join(" ")
        execute("docker manifest create --amend ${manifestName} ${manifests}")
        ARCH.each { k, v -> execute("docker manifest annotate --os linux --arch ${v} ${containerRegistry}${containerName}:${version} ${containerRegistry}${containerName}-${k}:${version}")}
    }

    void pushContainerManifest() {
        if(containerRegistry.length() > 0) {
            execute("docker manifest push --purge ${containerRegistry}${containerName}:${version}")
        }
    }
}

def container = new BuildContainer(project.getBasedir(), project.getVersion(), project.getName())
container.setupMultiarchBinaries()
container.buildContainerImages()
container.pushContainerImages()
container.buildContainerManifest()
container.pushContainerManifest()