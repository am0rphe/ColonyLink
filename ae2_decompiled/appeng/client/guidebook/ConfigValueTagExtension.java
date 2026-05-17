/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  guideme.compiler.PageCompiler
 *  guideme.compiler.tags.FlowTagCompiler
 *  guideme.document.flow.LytFlowParent
 *  guideme.libs.mdast.mdx.model.MdxJsxElementFields
 *  guideme.libs.unist.UnistNode
 */
package appeng.client.guidebook;

import appeng.core.AEConfig;
import guideme.compiler.PageCompiler;
import guideme.compiler.tags.FlowTagCompiler;
import guideme.document.flow.LytFlowParent;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;
import guideme.libs.unist.UnistNode;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ConfigValueTagExtension
extends FlowTagCompiler {
    public static final Map<String, Supplier<String>> CONFIG_VALUES = Map.of("crystalResonanceGeneratorRate", () -> String.valueOf(AEConfig.instance().getCrystalResonanceGeneratorRate()));

    public Set<String> getTagNames() {
        return Set.of("ae2:ConfigValue");
    }

    protected void compile(PageCompiler compiler, LytFlowParent parent, MdxJsxElementFields el) {
        String configValueName = el.getAttributeString("name", "");
        if (configValueName.isEmpty()) {
            parent.appendError(compiler, "name is required", (UnistNode)el);
            return;
        }
        Supplier<String> configValueSupplier = CONFIG_VALUES.get(configValueName);
        if (configValueSupplier == null) {
            parent.appendError(compiler, "unknown configuration value", (UnistNode)el);
            return;
        }
        parent.appendText(configValueSupplier.get());
    }
}

