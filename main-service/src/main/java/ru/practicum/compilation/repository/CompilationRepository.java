package ru.practicum.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.addition.MyPageRequest;
import ru.practicum.compilation.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long>  {
    List<Compilation> findByPinned(Boolean pinned, MyPageRequest pageRequest);
}
