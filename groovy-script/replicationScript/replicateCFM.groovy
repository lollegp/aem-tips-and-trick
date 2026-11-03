import com.day.cq.replication.Replicator
import com.day.cq.replication.ReplicationActionType
import com.day.cq.replication.ReplicationOptions
import com.day.cq.replication.AgentFilter
import javax.jcr.Session

// CONFIGURATION
def rootPath = "/conf/b2c-website-foundation/settings/dam/cfm/models"
def recursive = true
def dryRun = false  // Set to false to publish

def replicator = getService(Replicator)
def session = resourceResolver.adaptTo(Session)

def options = new ReplicationOptions()
options.setFilter({ agent ->
    agent.getId() == "preview" // Options: "preview", "publish",
} as AgentFilter)

def paths = []

def root = resourceResolver.getResource(rootPath)

if (root == null) {
    throw new IllegalArgumentException(
            "Resource not found: ${rootPath}"
    )
}

def collectModels
collectModels = { resource ->
    def content = resource.getChild("jcr:content")

    if (content != null &&
            content.valueMap.get("jcr:primaryType", "") ==
            "cq:PageContent" &&
            content.valueMap.get("sling:resourceType", "") ==
            "dam/cfm/models/console/components/data/entity/default") {
        paths.add(resource.path)
    }

    if (recursive) {
        resource.listChildren().each { child ->
            if (child.name != "jcr:content") {
                collectModels(child)
            }
        }
    }
}

if (root.getChild("jcr:content") != null) {
    collectModels(root)
} else {
    root.listChildren().each { collectModels(it) }
}

println "Found ${paths.size()} Content Fragment Models"

paths.each { path ->
    try {
        if (dryRun) {
            println "[DRY RUN] ${path}"
        } else {
            replicator.replicate(
                    session,
                    ReplicationActionType.ACTIVATE,
                    path,
                    options
            )
            println "[PREVIEW] Replication requested: ${path}"
        }
    } catch (Exception e) {
        println "[ERROR] ${path}: ${e.message}"
    }
}

println "Completed."
