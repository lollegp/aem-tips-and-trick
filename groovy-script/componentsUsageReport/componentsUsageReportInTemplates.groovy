package componentsUsageReport

import com.day.cq.dam.api.AssetManager
import org.apache.commons.lang3.StringUtils

import javax.jcr.RepositoryException

//------------------------------------------------------PARAMS----------------------------------------------------------
def rootPaths = ['/conf']
def brands = ['/example/de/', '/example/at/', '/example/ch/']
def confRoot = ['example-conf', 'example-conf', 'example-conf']
def rootFolderToSaveFile = "/content/dam/discovery-extraction-data/export-component"

//-------------------------------------------------------MAIN-----------------------------------------------------------
def csvString = new StringBuilder()
def statistic = [:]
def currentPage
def currentRootPath

def action = { node ->
    try {
        if (node.getName() != 'jcr:content') {
            def resourceType = node.getProperty('sling:resourceType').getString()
            if (StringUtils.contains(resourceType, "core-framework/")) {
                if (statistic.containsKey(resourceType)) {
                    statistic.get(resourceType).numberOfUsages++
                } else {
                    statistic.put(resourceType, new componentsUsageReport.Statistic())
                }
                if (statistic.get(resourceType)) {
                    for (String template : confRoot) {
                        if (StringUtils.contains(node.getPath(), template)) {
                            statistic.get(resourceType).brandsMap.put(template, true)
                        }
                    }
                }
            }

        } else if (node.hasProperty('cq:template')) {
            def path = node.getPath()
            currentPage = path.substring(path.indexOf(currentRootPath), path.indexOf('/jcr:content'))
        }
    } catch (RepositoryException e) {
        // node does not have resource type
    }
}

rootPaths.each {
    currentRootPath = it.substring(it.lastIndexOf('/'))
    traverseNodesRecursive(session.getNode(it), action)
}

def brandsHeader;


csvString.append("Component ResourceType,").append(StringUtils.join(brands, ",")).append("\n");
statistic.entrySet().each {
    def brandMatrix = new StringBuilder()

    for (String template : confRoot) {
        brandMatrix.append(it.value.brandsMap.get(template)).append(",")
    }

    def row = "${it.key},${brandMatrix}"
    csvString.append(StringUtils.joinWith(";", row)).append("\n")
}

def filePath = "${rootFolderToSaveFile}${currentRootPath}.csv"
convertFileToCsv(csvString, filePath)

class Statistic {
    int numberOfUsages = 1
    HashMap<String, Boolean> brandsMap = new HashMap<String, Boolean>()
}

//------------------------------------------------------UTILS-----------------------------------------------------------
def traverseNodesRecursive(Node node, action) {
    action(node)
    node.getNodes().each {
        traverseNodesRecursive(it, action)
    }
}

def convertFileToCsv(StringBuilder csvString, String filePath) {
    def targetStream = new ByteArrayInputStream(csvString.toString().getBytes())
    AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class)
    assetManager.createAsset(filePath, targetStream, "text/csv", true)
    println "Extraction Completed!!!"
    println "Path of generated file: ${filePath}"
}
