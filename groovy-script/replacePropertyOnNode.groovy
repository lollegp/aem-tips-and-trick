import org.apache.commons.lang3.StringUtils

getNode('/content/').recurse { resourceNode ->
    if (resourceNode.hasProperty('sling:resourceType')) {
        final def resourceType = resourceNode.getProperty('sling:resourceType').string
        if (StringUtils.contains(resourceType, "VALUE-TO-REPLACE")) {
            println "changing " + resourceNode.path
            final def newResourceType = StringUtils.replaceAll(resourceType, "OLD-VALUE", "NEW-VALUE");
            resourceNode.setProperty('sling:resourceType', newResourceType)
            resourceNode.save();
        }
    }
}
