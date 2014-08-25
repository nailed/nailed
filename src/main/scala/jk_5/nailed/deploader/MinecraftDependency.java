/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.deploader;

import java.io.File;

import jk_5.nailed.deploader.maven.MavenDependency;
import jk_5.nailed.deploader.maven.MavenResolver;

/**
 * No description given
 *
 * @author jk-5
 */
public class MinecraftDependency extends MavenDependency {

    public MinecraftDependency(String version) {
        super(new MavenResolver().addRepository("http://s3.amazonaws.com/Minecraft.Download/versions/"), null, null, version, null);
    }

    @Override
    public CharSequence getFolderPath() {
        return new StringBuilder().append(this.version).append('/').append("minecraft_server.").append(this.version).append(".jar");
    }

    @Override
    public File getLibraryLocation(File baseDir) {
        return new File(baseDir, "net/minecraft/minecraft_server/" + this.version + "/minecraft_server-" + this.version + ".jar");
    }
}
