import pytest

from cranium_core_v3 import LiveModelGenerator, SemanticEngine, cosine_sim, vec_add, vec_scale, vec_sub


def test_vector_helpers_are_deterministic():
    assert cosine_sim([1.0, 0.0], [1.0, 0.0]) == 1.0
    assert vec_add([1.0, 2.0], [3.0, 4.0]) == [4.0, 6.0]
    assert vec_sub([3.0, 4.0], [1.0, 2.0]) == [2.0, 2.0]
    assert vec_scale([2.0, -1.0], 0.5) == [1.0, -0.5]


def test_semantic_engine_tags_known_themes():
    engine = SemanticEngine()
    tags = engine.tags_for("preserve the memory logs and remember the past")
    assert "memory" in tags


def test_semantic_embedding_shape_and_normalization():
    vector = SemanticEngine().embed("meaning and connection")
    assert len(vector) == 32
    assert abs(cosine_sim(vector, vector) - 1.0) < 1e-9


def test_live_generator_refuses_unconfigured_mock_fallback(monkeypatch):
    monkeypatch.delenv("GEMINI_API_KEY", raising=False)
    generator = LiveModelGenerator()
    with pytest.raises(RuntimeError, match="no mock"):
        generator.generate("FIELD STATE")
