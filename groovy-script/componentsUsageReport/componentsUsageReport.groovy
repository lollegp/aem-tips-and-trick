package componentsUsageReport

import com.day.cq.dam.api.AssetManager
import org.apache.commons.lang3.StringUtils

import javax.jcr.RepositoryException

//------------------------------------------------------PARAMS----------------------------------------------------------
def rootPaths = ['/content/example', '/content/experience-fragments/example']
brands = ['/example/de/', '/example/at/', '/example/ch/']
def rootFolderToSaveFile = "/content/dam/usage-reports/export-component"

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
                    statistic.put(resourceType, new Statistic())
                }
                if (statistic.get(resourceType)) {
                    for (String brand : brands) {
                        if (StringUtils.contains(node.getPath(), brand)) {
                            statistic.get(resourceType).brandsMap.put(brand, true)
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
    Thread.sleep(1000)
}

def brandsHeader;


csvString.append("Component ResourceType,").append(StringUtils.join(brands, ",")).append("\n");
statistic.entrySet().each {
    def brandMatrix = new StringBuilder()
    for (String br: brands) {
        brandMatrix.append(it.value.brandsMap.get(br)).append(",")
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

