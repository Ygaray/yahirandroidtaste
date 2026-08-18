package com.example.secondbrain.yahirandroidtaste.component

/**
 * Downsample a list of amplitude samples to a target number of bars using peak-per-bucket.
 *
 * Used on the voice card face to fit stored samples into the available card width.
 * Each output bar represents the peak (max) amplitude in its bucket.
 *
 * @param samples Full-resolution normalized amplitude samples (0..1).
 * @param targetBars Number of output bars to produce.
 * @return Downsampled list of [targetBars] normalized amplitude values.
 *   Returns the original list unchanged if [samples].size <= [targetBars].
 *   Returns empty if [samples] is empty or [targetBars] <= 0.
 */
fun downsample(samples: List<Float>, targetBars: Int): List<Float> {
    if (samples.isEmpty() || targetBars <= 0) return emptyList()
    if (samples.size <= targetBars) return samples
    val bucketSize = samples.size / targetBars.toFloat()
    return List(targetBars) { i ->
        val start = (i * bucketSize).toInt()
        val end = ((i + 1) * bucketSize).toInt().coerceAtMost(samples.size)
        if (start >= end) 0f else samples.subList(start, end).max()
    }
}
