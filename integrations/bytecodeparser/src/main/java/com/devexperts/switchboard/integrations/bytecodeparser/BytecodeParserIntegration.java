package com.devexperts.switchboard.integrations.bytecodeparser;

import com.devexperts.switchboard.impl.IntegrationImpl;

/**
 * Integration for extracting tests from compiled classes via reflection
 */
public class BytecodeParserIntegration extends IntegrationImpl<BytecodeParserIntegration, BytecodeParserIntegrationFeatures, BytecodeParserIntegration.Builder> {
    private final BytecodeParserIntegrationFeatures features = new BytecodeParserIntegrationFeatures();

    private BytecodeParserIntegration() {}

    public BytecodeParserIntegration(Builder builder) {
        super(builder);
    }

    @Override
    public BytecodeParserIntegrationFeatures getIntegrationFeatures() {
        return features;
    }

    public static BytecodeParserIntegration.Builder newBuilder() {
        return new BytecodeParserIntegration.Builder();
    }

    public static class Builder extends IntegrationImpl.Builder<BytecodeParserIntegration, BytecodeParserIntegrationFeatures, Builder> {

        private Builder() {}

        @Override
        public BytecodeParserIntegration build() {
            return new BytecodeParserIntegration(this);
        }
    }
}
