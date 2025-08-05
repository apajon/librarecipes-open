#!/usr/bin/env python3
"""
Add sample baking recipes to LibraRecipes Mobile App database
"""

import sys
from pathlib import Path

# Add the app directory to Python path for imports
sys.path.append(str(Path(__file__).parent))

from src.db import get_db_session
from src.crud.recettes import create_recette

def add_sample_recipes():
    """Add sample baking recipes to the database"""
    
    sample_recipes = [
        {
            'nom': 'Chocolate Chip Cookies',
            'preparation': 15,
            'cuisson': 12,
            'portions': 24,
            'ingredients': [
                {'nom': 'All-purpose flour', 'quantite': '2 1/4', 'unite': 'cups', 'indispensable': True},
                {'nom': 'Baking soda', 'quantite': '1', 'unite': 'tsp', 'indispensable': True},
                {'nom': 'Salt', 'quantite': '1', 'unite': 'tsp', 'indispensable': True},
                {'nom': 'Butter', 'quantite': '1', 'unite': 'cup', 'indispensable': True},
                {'nom': 'Brown sugar', 'quantite': '3/4', 'unite': 'cup', 'indispensable': True},
                {'nom': 'White sugar', 'quantite': '3/4', 'unite': 'cup', 'indispensable': True},
                {'nom': 'Vanilla extract', 'quantite': '2', 'unite': 'tsp', 'indispensable': True},
                {'nom': 'Eggs', 'quantite': '2', 'unite': 'large', 'indispensable': True},
                {'nom': 'Chocolate chips', 'quantite': '2', 'unite': 'cups', 'indispensable': True},
            ],
            'etapes': [
                'Preheat oven to 375°F (190°C).',
                'Mix flour, baking soda, and salt in a bowl.',
                'Cream butter and both sugars until fluffy.',
                'Beat in vanilla and eggs one at a time.',
                'Gradually mix in flour mixture.',
                'Stir in chocolate chips.',
                'Drop rounded tablespoons onto ungreased cookie sheets.',
                'Bake 9-11 minutes until golden brown.',
                'Cool on baking sheet for 2 minutes, then transfer to wire rack.'
            ],
            'categories': ['Dessert', 'Cookies'],
            'tags': ['baking', 'chocolate', 'sweet', 'classic'],
            'source': {'type': 'homemade'}
        },
        {
            'nom': 'Banana Bread',
            'preparation': 15,
            'cuisson': 60,
            'portions': 8,
            'ingredients': [
                {'nom': 'All-purpose flour', 'quantite': '1 3/4', 'unite': 'cups', 'indispensable': True},
                {'nom': 'Baking soda', 'quantite': '1', 'unite': 'tsp', 'indispensable': True},
                {'nom': 'Salt', 'quantite': '1/2', 'unite': 'tsp', 'indispensable': True},
                {'nom': 'Butter', 'quantite': '1/3', 'unite': 'cup', 'indispensable': True},
                {'nom': 'Sugar', 'quantite': '2/3', 'unite': 'cup', 'indispensable': True},
                {'nom': 'Eggs', 'quantite': '2', 'unite': 'large', 'indispensable': True},
                {'nom': 'Ripe bananas', 'quantite': '3', 'unite': 'medium', 'indispensable': True},
                {'nom': 'Walnuts', 'quantite': '1/2', 'unite': 'cup', 'indispensable': False},
            ],
            'etapes': [
                'Preheat oven to 350°F (175°C). Grease a 9x5 inch loaf pan.',
                'Mix flour, baking soda, and salt in a large bowl.',
                'In another bowl, cream butter and sugar.',
                'Beat in eggs and mashed bananas.',
                'Stir banana mixture into flour mixture until just moistened.',
                'Fold in walnuts if using.',
                'Pour into prepared loaf pan.',
                'Bake 60-65 minutes until toothpick comes out clean.',
                'Cool in pan for 10 minutes, then turn out onto wire rack.'
            ],
            'categories': ['Bread', 'Dessert'],
            'tags': ['baking', 'banana', 'moist', 'comfort food'],
            'source': {'type': 'homemade'}
        },
        {
            'nom': 'Classic Vanilla Cupcakes',
            'preparation': 20,
            'cuisson': 18,
            'portions': 12,
            'ingredients': [
                {'nom': 'All-purpose flour', 'quantite': '1 1/2', 'unite': 'cups', 'indispensable': True},
                {'nom': 'Baking powder', 'quantite': '1 1/2', 'unite': 'tsp', 'indispensable': True},
                {'nom': 'Salt', 'quantite': '1/4', 'unite': 'tsp', 'indispensable': True},
                {'nom': 'Butter', 'quantite': '1/2', 'unite': 'cup', 'indispensable': True},
                {'nom': 'Sugar', 'quantite': '3/4', 'unite': 'cup', 'indispensable': True},
                {'nom': 'Eggs', 'quantite': '2', 'unite': 'large', 'indispensable': True},
                {'nom': 'Vanilla extract', 'quantite': '2', 'unite': 'tsp', 'indispensable': True},
                {'nom': 'Milk', 'quantite': '3/4', 'unite': 'cup', 'indispensable': True},
            ],
            'etapes': [
                'Preheat oven to 350°F (175°C). Line 12 muffin cups with paper liners.',
                'Whisk flour, baking powder, and salt in a bowl.',
                'Beat butter and sugar until light and fluffy.',
                'Add eggs one at a time, then vanilla.',
                'Alternate adding flour mixture and milk, beginning and ending with flour.',
                'Divide batter among prepared cups.',
                'Bake 18-20 minutes until golden and toothpick comes out clean.',
                'Cool in pan 5 minutes, then transfer to wire rack.'
            ],
            'categories': ['Dessert', 'Cupcakes'],
            'tags': ['baking', 'vanilla', 'party', 'celebration'],
            'source': {'type': 'homemade'}
        },
        {
            'nom': 'Lemon Bars',
            'preparation': 25,
            'cuisson': 45,
            'portions': 16,
            'ingredients': [
                {'nom': 'All-purpose flour', 'quantite': '2', 'unite': 'cups', 'indispensable': True},
                {'nom': 'Butter', 'quantite': '1/2', 'unite': 'cup', 'indispensable': True},
                {'nom': 'Powdered sugar', 'quantite': '1/2', 'unite': 'cup', 'indispensable': True},
                {'nom': 'Eggs', 'quantite': '4', 'unite': 'large', 'indispensable': True},
                {'nom': 'White sugar', 'quantite': '1 1/2', 'unite': 'cups', 'indispensable': True},
                {'nom': 'Fresh lemon juice', 'quantite': '1/4', 'unite': 'cup', 'indispensable': True},
                {'nom': 'Lemon zest', 'quantite': '1', 'unite': 'tbsp', 'indispensable': True},
                {'nom': 'Salt', 'quantite': '1/4', 'unite': 'tsp', 'indispensable': True},
            ],
            'etapes': [
                'Preheat oven to 350°F (175°C). Grease a 9x13 inch pan.',
                'Mix 1 1/2 cups flour, butter, and powdered sugar for crust.',
                'Press mixture into prepared pan.',
                'Bake crust 20 minutes until lightly golden.',
                'Beat eggs, white sugar, lemon juice, zest, remaining flour, and salt.',
                'Pour lemon mixture over hot crust.',
                'Bake 25 minutes until set and lightly golden.',
                'Cool completely before cutting into bars.',
                'Dust with powdered sugar before serving.'
            ],
            'categories': ['Dessert', 'Bars'],
            'tags': ['baking', 'lemon', 'citrus', 'tangy', 'summer'],
            'source': {'type': 'homemade'}
        },
        {
            'nom': 'Homemade Pizza Dough',
            'preparation': 20,
            'cuisson': 0,
            'portions': 4,
            'ingredients': [
                {'nom': 'Warm water', 'quantite': '1', 'unite': 'cup', 'indispensable': True},
                {'nom': 'Active dry yeast', 'quantite': '1', 'unite': 'packet', 'indispensable': True},
                {'nom': 'Sugar', 'quantite': '1', 'unite': 'tsp', 'indispensable': True},
                {'nom': 'All-purpose flour', 'quantite': '3', 'unite': 'cups', 'indispensable': True},
                {'nom': 'Salt', 'quantite': '1', 'unite': 'tsp', 'indispensable': True},
                {'nom': 'Olive oil', 'quantite': '2', 'unite': 'tbsp', 'indispensable': True},
            ],
            'etapes': [
                'Combine warm water, yeast, and sugar in a large bowl.',
                'Let stand 5 minutes until foamy.',
                'Add flour, salt, and olive oil.',
                'Mix until a dough forms.',
                'Knead on floured surface 8-10 minutes until smooth.',
                'Place in oiled bowl, cover, and let rise 1 hour.',
                'Punch down dough and divide into 4 portions.',
                'Roll out each portion for pizza bases.',
                'Use immediately or freeze for later use.'
            ],
            'categories': ['Bread', 'Pizza'],
            'tags': ['yeast', 'savory', 'versatile', 'freezer-friendly'],
            'source': {'type': 'homemade'}
        }
    ]
    
    try:
        with get_db_session() as session:
            for recipe_data in sample_recipes:
                recipe = create_recette(session, recipe_data)
                print(f"Added recipe: {recipe.nom}")
        
        print(f"\n✅ Successfully added {len(sample_recipes)} sample recipes!")
        
    except Exception as e:
        print(f"❌ Error adding sample recipes: {e}")
        import traceback
        traceback.print_exc()

if __name__ == '__main__':
    add_sample_recipes()