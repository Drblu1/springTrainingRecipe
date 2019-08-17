package guru.springframework.services;

import guru.springframework.commands.IngredientCommand;
import guru.springframework.converters.IngredientCommandToIngredient;
import guru.springframework.converters.IngredientToIngredientCommand;
import guru.springframework.domain.Ingredient;
import guru.springframework.domain.Recipe;
import guru.springframework.repositories.RecipeRepository;
import guru.springframework.repositories.UnitOfMeasureRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientToIngredientCommand ingredientToIngredientCommand;
    private final RecipeRepository recipeRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final IngredientCommandToIngredient ingredientCommandToIngredient;

    public IngredientServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand, RecipeRepository recipeRepository, UnitOfMeasureRepository unitOfMeasureRepository, IngredientCommandToIngredient ingredientCommandToIngredient) {
        this.ingredientToIngredientCommand = ingredientToIngredientCommand;
        this.recipeRepository = recipeRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.ingredientCommandToIngredient = ingredientCommandToIngredient;
    }

    @Override
    public IngredientCommand findByRecipeIdAndIngredientId(Long recipeId, Long ingredientId) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RuntimeException("no recipe found for this id " + recipeId));

        return recipe.getIngredients().stream().filter(ingredient -> ingredientId.equals(ingredient.getId()))
                .map(ingredientToIngredientCommand::convert).findFirst().orElseThrow(() -> new RuntimeException("No ingredient found for this recipe id " + recipeId + " and ingredient id " + ingredientId));
    }

    @Override
    @Transactional
    public IngredientCommand saveIngredientCommand(IngredientCommand ingredientCommand) {
        Optional<Recipe> recipeOptional = recipeRepository.findById(ingredientCommand.getRecipeId());

        if (!recipeOptional.isPresent()) {
            return new IngredientCommand();
        } else {
            Recipe recipe = recipeOptional.get();

            Optional<Ingredient> ingredientOptional = recipe.getIngredients()
                    .stream()
                    .filter(ingredient -> ingredient.getId().equals(ingredientCommand.getId()))
                    .findFirst();

            if (ingredientOptional.isPresent()) {
                Ingredient ingredientFound = ingredientOptional.get();
                ingredientFound.setDescription(ingredientCommand.getDescription());
                ingredientFound.setAmount(ingredientCommand.getAmount());
                Long unitOfMesureId = ingredientCommand.getUnitOfMeasure().getId();
                ingredientFound.setUnitOfMeasure(unitOfMeasureRepository.findById(unitOfMesureId).orElseThrow(() -> new RuntimeException("No unit of mesure found")));
            } else
            {
                Ingredient ingredient = ingredientCommandToIngredient.convert(ingredientCommand);
                ingredient.setRecipe(recipe);
                recipe.addIngredient(ingredient);
            }

            Recipe savedRecipe = recipeRepository.save(recipe);
            Optional<Ingredient> savedOptionalIngredient = savedRecipe.getIngredients().stream().filter(ingredient -> ingredient.getId().equals(ingredientCommand.getId())).findFirst();

            if(!savedOptionalIngredient.isPresent()) {
                savedOptionalIngredient = savedRecipe.getIngredients().stream()
                        .filter( ingredient -> ingredient.getDescription().equals(ingredientCommand.getDescription()))
                        .filter( ingredient -> ingredient.getAmount().equals(ingredientCommand.getAmount()))
                        .filter( ingredient -> ingredient.getUnitOfMeasure().getId().equals(ingredientCommand.getUnitOfMeasure().getId()))
                        .findFirst();
            }
            return  ingredientToIngredientCommand.convert(savedOptionalIngredient.get());
        }
    }

    @Override
    public void deleteById(Long recipeId, Long ingredientId) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RuntimeException("no recipe for that id" + ingredientId));
        boolean isRemoved = recipe.getIngredients().removeIf(ingredient -> {
            if(ingredientId.equals(ingredient.getId())){
                ingredient.setRecipe(null);
                return true;
            }
            return false;
        });
        if(!isRemoved) {
            throw new RuntimeException("no ingredient to remove");
        }
        recipeRepository.save(recipe);
    }
}
