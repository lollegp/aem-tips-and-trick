import groovy.json.JsonOutput
import javax.jcr.Node

final String POLICY_ROOT = "/conf/b2c-website-foundation/settings/wcm/policies/b2c-website-foundation/components"
def slurper = new groovy.json.JsonSlurper()
def outputList = []

// Extract individual style entries
def extractStyles = { Node styleGroupNode ->
    def styles = []
    if (styleGroupNode.hasNodes()) {
        styleGroupNode.nodes.each { styleNode ->
            if (styleNode.name == "cq:styles") {
                styleNode.nodes.each { cqStyle ->
                    println "inside stylenode: " + cqStyle
                    styles << [
                        title    : cqStyle.hasProperty("cq:styleLabel") ? cqStyle.getProperty("cq:styleLabel").string : null,
                        cssClass : cqStyle.hasProperty("cq:styleClasses") ? cqStyle.getProperty("cq:styleClasses").string : null,
                        styleId : cqStyle.hasProperty("cq:styleId") ? cqStyle.getProperty("cq:styleId").string : null
                    ]
                }
              
            }
        }
    }
    return styles
}

// Extract the groups for a given policy node
def extractStyleGroups = { Node policyNode ->
    def groups = []
    if (policyNode.hasNode("cq:styleGroups")) {
        def groupsNode = policyNode.getNode("cq:styleGroups")
        groupsNode.nodes.each { groupNode ->
            groups << [
                title : groupNode.hasProperty("cq:styleGroupLabel") ? groupNode.getProperty("cq:styleGroupLabel").string : null,
                styles: extractStyles(groupNode)
            ]
        }
    }
    return groups
}

// Traverse components and their policy nodes
def traverse = { Node rootNode ->
    rootNode.nodes.each { componentNode ->
        def componentName = componentNode.name
        componentNode.nodes.each { policyNode ->
            def styleGroups = extractStyleGroups(policyNode)
            if (!styleGroups.isEmpty()) {
                outputList << [
                    component   : componentName,
                    policyId    : policyNode.name,
                    policyPath  : policyNode.path,
                    styleGroups : styleGroups
                ]
            }
        }
    }
}

// Entry point
def rootNode = getNode(POLICY_ROOT)
if (rootNode != null) {
    traverse(rootNode)
} else {
    return "❌ Error: Could not find node at ${POLICY_ROOT}"
}

return JsonOutput.prettyPrint(JsonOutput.toJson(outputList))
