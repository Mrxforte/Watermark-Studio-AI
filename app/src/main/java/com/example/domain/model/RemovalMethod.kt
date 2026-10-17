package com.example.domain.model

enum class RemovalMethod(
    val title: String,
    val category: MethodCategory,
    val description: String,
    val strengths: String,
    val limitations: String
) {
    TELEA(
        title = "Telea Fast Marching",
        category = MethodCategory.SPATIAL_RECONSTRUCTION,
        description = "Propagates boundary gradient information inward using the Fast Marching Method (Telea 2004).",
        strengths = "Fast execution, sharp edges, ideal for thin text, channel logos, and static corner stamps.",
        limitations = "May introduce slight radial blur on very large textured areas."
    ),
    NAVIER_STOKES(
        title = "Navier-Stokes PDE",
        category = MethodCategory.SPATIAL_RECONSTRUCTION,
        description = "Preserves continuous isophote direction lines using fluid dynamic partial differential equations (Bertalmio et al.).",
        strengths = "Maintains smooth gradient transitions across natural backgrounds, skies, and continuous tones.",
        limitations = "Slightly higher compute cost than Telea; can soften sharp high-frequency textures."
    ),
    TEMPORAL(
        title = "Temporal Reconstruction",
        category = MethodCategory.TEMPORAL_RECONSTRUCTION,
        description = "Reconstructs occluded pixels by searching neighboring video frames with background alignment.",
        strengths = "Achieves near-perfect reconstruction when camera or background motion exposes hidden content.",
        limitations = "Requires sufficient camera or subject movement to reveal occluded background."
    ),
    GAUSSIAN_BLUR(
        title = "Gaussian Concealment",
        category = MethodCategory.VISUAL_CONCEALMENT,
        description = "Applies feathered Gaussian blur over the selected region to conceal sensitive information.",
        strengths = "100% predictable, lightning-fast rendering, zero artifacts.",
        limitations = "Visually noticeable blurred patch; does not restore underlying image."
    ),
    PIXELATE_MOSAIC(
        title = "Mosaic / Pixelate",
        category = MethodCategory.VISUAL_CONCEALMENT,
        description = "Downsamples the watermark area into pixelated grid blocks with smooth edge transitions.",
        strengths = "Classic broadcast censorship style, very fast processing.",
        limitations = "Conceals without restoring missing pixels."
    ),
    SMART_CROP(
        title = "Smart Crop",
        category = MethodCategory.VISUAL_CONCEALMENT,
        description = "Adjusts aspect ratio or crops the video canvas slightly to eliminate edge/corner watermarks.",
        strengths = "No visible blurring or inpainting seam in the final video.",
        limitations = "Discards edge content of the frame."
    ),
    LOGO_REPLACE(
        title = "Brand Overlay",
        category = MethodCategory.VISUAL_CONCEALMENT,
        description = "Replaces the original watermark with user-defined logo, banner plate, or signature.",
        strengths = "Clean branding opportunity, hides underlying watermark completely.",
        limitations = "Leaves a new visual badge on the video."
    )
}

enum class MethodCategory {
    SPATIAL_RECONSTRUCTION,
    TEMPORAL_RECONSTRUCTION,
    VISUAL_CONCEALMENT
}
