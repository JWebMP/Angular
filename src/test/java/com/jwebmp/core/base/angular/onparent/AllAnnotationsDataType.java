package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgDataType;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorBody;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.annotations.functions.*;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportProvider;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.*;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDataType;

/**
 * Comprehensive test fixture that declares every annotation type that has onParent=true support.
 * Used as a referenced class via @NgComponentReference to verify all annotations propagate correctly.
 */
@NgDataType(NgDataType.DataTypeClass.Class)

// ─── Import References ─────────────────────────────────────────────────────────
@NgImportReference(value = "Injectable", reference = "@angular/core")
@NgImportReference(value = "inject", reference = "@angular/core", onParent = true, onSelf = false)
@NgImportReference(value = "EventEmitter", reference = "@angular/core", onParent = true, onSelf = false)

// ─── Import Module ─────────────────────────────────────────────────────────────
@NgImportModule(value = "CommonModule", onParent = true, onSelf = false)

// ─── Import Provider ───────────────────────────────────────────────────────────
@NgImportProvider(value = "AllAnnotationsService", onParent = true, onSelf = false)

// ─── Fields ────────────────────────────────────────────────────────────────────
@NgField(value = "allAnnotations: AllAnnotationsDataType = inject(AllAnnotationsDataType);", onParent = true, onSelf = false)
@NgField(value = "private selfOnlyField: string = 'self';", onParent = false, onSelf = true)

// ─── Global Fields ─────────────────────────────────────────────────────────────
@NgGlobalField(value = "const ALL_ANNOTATIONS_GLOBAL = 'propagated';", onParent = true, onSelf = false)

// ─── Signals ───────────────────────────────────────────────────────────────────
@NgSignal(value = "0", type = "number", referenceName = "parentCounter", onParent = true, onSelf = false)

// ─── Methods ───────────────────────────────────────────────────────────────────
@NgMethod(value = """
        parentMethod(): void {
            console.log('from parent');
        }""", onParent = true, onSelf = false)
@NgMethod(value = """
        selfMethod(): void {
            console.log('self only');
        }""", onParent = false, onSelf = true)

// ─── Interfaces ────────────────────────────────────────────────────────────────
@NgInterface(value = "OnInit", onParent = true, onSelf = false)

// ─── Injects ───────────────────────────────────────────────────────────────────
@NgInject(value = "AllAnnotationsDataType", referenceName = "allAnno", onParent = true, onSelf = false)

// ─── Constructor Parameters ────────────────────────────────────────────────────
@NgConstructorParameter(value = "private parentInjected: AllAnnotationsDataType", onParent = true, onSelf = false)
@NgConstructorParameter(value = "private selfInjected: string", onParent = false, onSelf = true)

// ─── Constructor Bodies ────────────────────────────────────────────────────────
@NgConstructorBody(value = "console.log('parent constructor body');", onParent = true, onSelf = false)
@NgConstructorBody(value = "console.log('self constructor body');", onParent = false, onSelf = true)

// ─── Lifecycle: OnInit ─────────────────────────────────────────────────────────
@NgOnInit(value = "console.log('parent onInit');", onParent = true, onSelf = false)

// ─── Lifecycle: OnDestroy ──────────────────────────────────────────────────────
@NgOnDestroy(value = "console.log('parent onDestroy');", onParent = true, onSelf = false)

// ─── Lifecycle: AfterViewInit ──────────────────────────────────────────────────
@NgAfterViewInit(value = "console.log('parent afterViewInit');", onParent = true, onSelf = false)

// ─── Lifecycle: AfterViewChecked ───────────────────────────────────────────────
@NgAfterViewChecked(value = "console.log('parent afterViewChecked');", onParent = true, onSelf = false)

// ─── Lifecycle: AfterContentInit ───────────────────────────────────────────────
@NgAfterContentInit(value = "console.log('parent afterContentInit');", onParent = true, onSelf = false)

// ─── Lifecycle: AfterContentChecked ────────────────────────────────────────────
@NgAfterContentChecked(value = "console.log('parent afterContentChecked');", onParent = true, onSelf = false)

public class AllAnnotationsDataType implements INgDataType<AllAnnotationsDataType>
{
    @Override
    public String renderBeforeClass()
    {
        return "@Injectable({ providedIn: 'root' })\n";
    }
}

