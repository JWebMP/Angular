package com.jwebmp.core.base.angular.typescript;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.JWebMPTypeScriptCompiler;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.getTsFilename;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.getClassDirectory;

public class JWebMPTypeScriptCompilerTest
{
    public static void main(String[] args) throws Exception
    {
        IGuiceContext.instance()
                     .inject();
    }

    @Test
    public void testAppSearch() throws IOException
    {
        IGuiceContext.instance()
                     .inject();
        for (INgApp<?> app : JWebMPTypeScriptCompiler.getAllApps())
        {
            JWebMPTypeScriptCompiler compiler = new JWebMPTypeScriptCompiler(app);
            compiler.renderAppTS((Class<? extends INgApp<?>>) app.getClass());
            System.out.println("Generating @NgApp (" + getTsFilename(app.getClass()) + ") " +
                                       "in folder " + getClassDirectory(app.getClass()));
            System.out.println("================");
            //compiler.renderAppTS(app);
            System.out.println("================");
        }
    }
}