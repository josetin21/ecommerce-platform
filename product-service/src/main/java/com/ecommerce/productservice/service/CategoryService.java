package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.CreateCategoryRequest;
import com.ecommerce.productservice.dto.response.CategoryResponse;
import com.ecommerce.productservice.entity.Category;
import com.ecommerce.productservice.exception.CategoryNotFoundException;
import com.ecommerce.productservice.mapper.CategoryMapper;
import com.ecommerce.productservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> getAllCategories(){
        return categoryRepository.findAllRootCategoriesWithChildren()
                .stream()
                .map(this::mapWithChildren)
                .toList();
    }

    public CategoryResponse getCategoryById(UUID id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));

        return mapWithChildren(category);
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request){
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        if (request.getParentId() != null){
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException(
                            request.getParentId().toString()));
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        log.info("Category created: {}", saved.getName());
        return categoryMapper.toCategoryResponse(saved);
    }

    @Transactional
    public void deleteCategory(UUID id){
        if (!categoryRepository.existsById(id)){
            throw new CategoryNotFoundException(id.toString());
        }
        categoryRepository.deleteById(id);
        log.info("Category deleted: {}", id);
    }

    private CategoryResponse mapWithChildren(Category category){
        CategoryResponse response = categoryMapper.toCategoryResponse(category);
        if (category.getChildren() != null && !category.getChildren().isEmpty()){
            response.setChildren(
                    category.getChildren().stream()
                            .map(categoryMapper::toCategoryResponse)
                            .toList()
            );
        }
        return response;
    }
}
