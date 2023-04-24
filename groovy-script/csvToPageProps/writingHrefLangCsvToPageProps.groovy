package csvToPageProps

import org.apache.commons.lang3.StringUtils

InputStream is = new ByteArrayInputStream(data.getBytes("utf-8"))
BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
def homepageRoot = "/content/example/de_de"
def domain = "https://www.example.de"

br.eachLine { line ->
    def arrayOfStrings = line.split(";")
    if (arrayOfStrings.length > 1) {
        def pageName = StringUtils.replace(arrayOfStrings[0], domain, "")
        pageName = StringUtils.replace(pageName, ".html", "")
        def pageResource = resourceResolver.getResource(homepageRoot + pageName + "/jcr:content")
        if (pageResource != null) {
            def modifiableValueMap = pageResource.adaptTo(ModifiableValueMap.class)
            def hrefLangTag = ""
            for (arrayOfStringsIndex = 1; arrayOfStringsIndex < arrayOfStrings.length; arrayOfStringsIndex = arrayOfStringsIndex + 2) {
                def hreflangCountry = arrayOfStrings[arrayOfStringsIndex]
                def hreflangValue = arrayOfStrings[arrayOfStringsIndex + 1]
                if (StringUtils.isNotBlank(hreflangCountry) && StringUtils.isNotBlank(hreflangValue)) {
                    hrefLangTag = hrefLangTag + "<link rel=\"alternate\" hreflang=\"${StringUtils.replace(hreflangCountry, "_", "-")}\" href=\"${hreflangValue}\">\n"
                }
            }
            println "Adding following hreflang to page: ${homepageRoot + pageName}"
            println hrefLangTag
            modifiableValueMap.put("customHrefLang", hrefLangTag)
            resourceResolver.commit()
        }
    }
}

