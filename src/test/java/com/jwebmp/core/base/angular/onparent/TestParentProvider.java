package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgProvider;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.annotations.structures.NgMethod;
import com.jwebmp.core.base.angular.client.annotations.structures.NgSignal;
import com.jwebmp.core.base.angular.client.services.interfaces.INgProvider;

/**
 * A test provider that declares fields, imports, constructor params, and methods with onParent=true.
 * When referenced via @NgComponentReference from a parent component, these should propagate up.
 */
@NgProvider
@NgImportReference(value = "Injectable", reference = "@angular/core")
@NgImportReference(value = "inject", reference = "@angular/core", onParent = true, onSelf = false)
@NgField(value = "testProvider: TestParentProvider = inject(TestParentProvider);", onParent = true, onSelf = false)
@NgField(value = "private internalState: string = '';", onParent = false, onSelf = true)
@NgMethod(value = """
        getState(): string {
            return this.internalState;
        }""", onParent = false, onSelf = true)
@NgMethod(value = """
        refreshProvider(): void {
            this.testProvider.getState();
        }""", onParent = true, onSelf = false)
@NgConstructorParameter(value = "private http: HttpClient", onParent = false, onSelf = true)
@NgConstructorParameter(value = "private providerParam: TestParentProvider", onParent = true, onSelf = false)
@NgSignal(value = "false", type = "boolean", referenceName = "providerReady", onParent = true, onSelf = false)
public class TestParentProvider implements INgProvider<TestParentProvider>
{
}

