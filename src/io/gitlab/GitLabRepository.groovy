#!groovy
package io.gitlab

import java.util.ArrayList
import java.lang.String
import java.net.URL
import java.net.URLConnection
import groovy.json.JsonSlurper

class ReleaseAuthor {

    final private String name
    final private String username

    ReleaseAuthor(String name, String username) {
        this.name = name
        this.username = username
    }

    static ReleaseAuthor fromJson(Object jsonObject) {
        return new ReleaseAuthor(
            jsonObject['name'],
            jsonObject['username']
        )
    }

    public String toString() {
        return this.name
    }

}

class Release {

    final private String name
    final private String tagName
    final private ReleaseAuthor author

    Release(String name, String tagName, ReleaseAuthor author) {
        this.name = name
        this.tagName = tagName
        this.author = author
    }

    static Release fromJson(Object jsonObject) {
        return new Release(
            jsonObject['name'],
            jsonObject['tag_name'],
            ReleaseAuthor.fromJson(
                jsonObject['author']
            )
        )
    }

    public String toString() {
        return 'Релиз: ' + this.name + '/' + this.tagName + 'Автор: ' + this.author.toString()
    }

}

class GitLabRepository {

    final private String protocol
    final private String baseURL
    final private String privateAccessToken
    final private String baseAPIV4

    GitLabRepository(String protocol, String baseURL, String privateAccessToken) {
        this.protocol = protocol
        this.baseURL = baseURL
        this.privateAccessToken = privateAccessToken
        this.baseAPIV4 = this.protocol + '://' + this.baseURL + '/api/v4'
    }

    public Integer getRepositoryID(String repositoryDomain, String repositoryName) {
        // Example: https://gitlab.com/api/v4/projects/team-building3%2Fgolnag
        def url = this.baseAPIV4 + '/projects/' + repositoryDomain + '%2F' + repositoryName
        def jsonObject = this.getRequest(url)
        return jsonObject['id']
    }

    private def getRequest(String url) {
        def baseUrl = new URL(url)
        HttpURLConnection connection = (HttpURLConnection) baseUrl.openConnection()
        connection.setRequestMethod('GET')
        connection.addRequestProperty('Content-Type', 'application/json')
        connection.addRequestProperty('PRIVATE-TOKEN', this.privateAccessToken)
        connection.setDoOutput(true)
        if (connection.responseCode == 200) {
            def slurper = new JsonSlurper()
            def result = slurper.parseText(connection.content.text)
            return result
        }
        return null
    }

    public List<Release> getReleases(Integer repositoryId) {
        def url = this.baseAPIV4 + "/projects/$repositoryId/releases"
        def jsonArrayObject = this.getRequest(url)
        List<Release> releases = []
        for (jsonObject in jsonArrayObject) releases.add(Release.fromJson(jsonObject))
        return releases
    }

    public List<String> getBranches(Integer repositoryId) {
        def url = this.baseAPIV4 + "/projects/$repositoryId/repository/branches"
        def jsonArrayObject = this.getRequest(url)
        List<String> branch  = []
        for (jsonObject in jsonArrayObject) branch.add(jsonObject['name'])
        return branch
    }

}
