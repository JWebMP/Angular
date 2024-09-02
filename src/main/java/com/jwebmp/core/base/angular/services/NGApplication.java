package com.jwebmp.core.base.angular.services;

import com.jwebmp.core.Page;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDependency;
import com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.JWebMPTypeScriptCompiler;
import com.jwebmp.core.base.html.Base;
import com.jwebmp.core.base.html.Meta;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.getTsFilename;
import static com.jwebmp.core.base.angular.client.services.interfaces.ImportsStatementsComponent.getRelativePath;

@TsDependency(value = "@angular/platform-browser", version = "^18.0.1", overrides = true)
@TsDependency(value = "@angular/platform-browser-dynamic", version = "^18.0.1")

//@NgImportReference(value = "platformBrowserDynamic", reference = "@angular/platform-browser-dynamic")
//@NgImportReference(value = "enableProdMode", reference = "@angular/core")
//@NgComponentReference(EnvironmentModule.class)
public class NGApplication<J extends NGApplication<J>> extends Page<J> implements INgApp<J>
{
    private List<String> renderAfterImports;

    @Getter
    @Setter
    private List<IComponentHierarchyBase<?, ?>> routes = new ArrayList<>();

    public NGApplication()
    {
        getHead()
                .add(new Meta(Meta.MetadataFields.Charset, "utf-8"));
        getHead()
                .add(new Meta(Meta.MetadataFields.ViewPort, "width=device-width, initial-scale=1"));
        getOptions().setBase(new Base<>("/"));

        if (getClass().isAnnotationPresent(NgApp.class))
        {
            addConfiguration(AnnotationUtils.getNgComponentReference(getClass().getAnnotation(NgApp.class)
                                                                               .bootComponent()));
        }
    }


    public List<String> getRenderAfterImports()
    {
        if (renderAfterImports == null)
        {
            renderAfterImports = new ArrayList<>();
        }
        return renderAfterImports;
    }

    public NGApplication setRenderAfterImports(List<String> renderAfterImports)
    {
        this.renderAfterImports = renderAfterImports;
        return this;
    }

    @Override
    public List<NgImportReference> putRelativeLinkInMap(Class<?> clazz, NgComponentReference moduleRef)
    {
        List<NgImportReference> out = new ArrayList<>();
        var baseDir = JWebMPTypeScriptCompiler.getCurrentAppFile();
        try
        {
            File me = new File(baseDir.get()
                                      .getCanonicalPath()
                                      .replace('\\', '/') + "/src");
            File destination = new File(getFileReference(baseDir.get()
                                                                .getCanonicalPath(), moduleRef.value()));
            out.add(AnnotationUtils.getNgImportReference(getTsFilename(moduleRef.value()), getRelativePath(me, destination, null)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return out;
    }
}
