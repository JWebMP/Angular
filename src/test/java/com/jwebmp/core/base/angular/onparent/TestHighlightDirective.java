package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgDirective;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorBody;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.annotations.structures.NgMethod;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDirective;

/**
 * A test directive that exposes annotations with onParent=true.
 * When a component references this directive, the onParent items should appear on the component.
 */
@NgDirective("[testHighlight]")
@NgImportReference(value = "Renderer2", reference = "@angular/core", onParent = true, onSelf = false)
@NgField(value = "private highlightActive: boolean = false;", onParent = true, onSelf = false)
@NgMethod(value = """
        toggleHighlight(): void {
            this.highlightActive = !this.highlightActive;
        }""", onParent = true, onSelf = false)
@NgConstructorBody(value = "console.log('highlight directive initialized');", onParent = true, onSelf = false)
public class TestHighlightDirective implements INgDirective<TestHighlightDirective>
{
}

