-- Brand Voice Service Database Schema
-- Version: 1.0
-- Description: Initial schema for brand profiles

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create brand_profiles table
CREATE TABLE brand_profiles (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                customer_id VARCHAR(255) UNIQUE NOT NULL,
                                brand_name VARCHAR(255) NOT NULL,
                                voice_attributes JSONB NOT NULL,
                                sample_content TEXT,
                                confidence_score DECIMAL(3,2),
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                active BOOLEAN DEFAULT true
);

-- Create indexes for performance
CREATE INDEX idx_customer_id ON brand_profiles(customer_id);
CREATE INDEX idx_active ON brand_profiles(active);
CREATE INDEX idx_created_at ON brand_profiles(created_at DESC);
CREATE INDEX idx_voice_attributes ON brand_profiles USING GIN (voice_attributes);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for updated_at
CREATE TRIGGER update_brand_profiles_updated_at
    BEFORE UPDATE ON brand_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE brand_profiles IS 'Stores brand voice profiles for customers';
COMMENT ON COLUMN brand_profiles.voice_attributes IS 'JSONB field storing tone, formality, vocabulary complexity, etc.';
COMMENT ON COLUMN brand_profiles.confidence_score IS 'Score between 0 and 1 indicating profile confidence';