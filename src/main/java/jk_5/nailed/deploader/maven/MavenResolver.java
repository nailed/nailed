package jk_5.nailed.deploader.maven;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * No description given
 *
 * @author jk-5
 */
public class MavenResolver {

    Set<String> repositories = new LinkedHashSet<String>();

    public MavenResolver addRepository(String url){
        if(!url.endsWith("/")) url += "/";
        repositories.add(url);
        return this;
    }

    public MavenResolver addMavenCentral(){
        this.addRepository("http://repo1.maven.org/maven2/");
        return this;
    }

    public MavenDependency dependency(String mavenPath){
        if(mavenPath == null) throw new IllegalArgumentException("mavenPath may not be null!");

        String[] s1 = mavenPath.split(":");
        String group = s1[0];
        String artifact = s1[1];
        String version = s1[2];
        String classifier = s1.length > 3 ? s1[0] : null;

        return new MavenDependency(this, group, artifact, version, classifier);
    }
}
