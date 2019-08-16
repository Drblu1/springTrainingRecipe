package guru.springframework.services;

import guru.springframework.domain.Recipe;
import guru.springframework.repositories.RecipeRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecipeServiceImplTest {

    private RecipeServiceImpl recipeService;
    
    @Mock
    private RecipeRepository recipeRepository;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        recipeService = new RecipeServiceImpl(recipeRepository, recipeCommandToRecipe, recipeToRecipeCommand);
    }

    @Test
    public void getRecispes() {
        Recipe recipe = new Recipe();
        HashSet recipesData = new HashSet();
        recipesData.add(recipe);
        when(recipeRepository.findAll()).thenReturn(recipesData);

        Set<Recipe> recipes = recipeService.getRecipes();

        assertEquals(recipesData, recipes);
    }

    @Test
    public void should_ask_for_entity_to_repository_when_recipe_service_ask_for_entity_by_id() {
        long id = 1597L;
        Optional<Recipe> optionalRecipe = Optional.of(new Recipe());
        when(recipeRepository.findById(id)).thenReturn(optionalRecipe);

        recipeService.findById(id);

        verify(recipeRepository).findById(id);
    }

    @Test
    public void should_return_a_recipe_when_recipe_service_ask_for_a_recipe_by_id() {
        long id = 1545L;
        Recipe expectedRecipe = new Recipe();
        expectedRecipe.setId(id);
        Optional<Recipe> optionalRecipe = Optional.of(expectedRecipe);
        when(recipeRepository.findById(id)).thenReturn(optionalRecipe);

        Recipe recipe = recipeService.findById(id);

        assertEquals(expectedRecipe, recipe);
    }
}