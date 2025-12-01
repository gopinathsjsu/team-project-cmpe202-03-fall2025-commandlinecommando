-- V14: Add image columns to listings table for S3 image upload support
-- This migration adds support for storing multiple image URLs per listing

-- Add image_urls column (JSON array of S3 URLs)
ALTER TABLE listings ADD COLUMN IF NOT EXISTS image_urls TEXT;

-- Add primary_image_url column (main display image)
ALTER TABLE listings ADD COLUMN IF NOT EXISTS primary_image_url VARCHAR(500);

