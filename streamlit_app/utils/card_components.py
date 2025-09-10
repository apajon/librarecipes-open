"""
Card components for a better UI layout
"""
import streamlit as st


def create_card(title, content_func, icon="", expanded=True, key=None):
    """
    Create a card-like container using Streamlit's expander
    
    Args:
        title: Card title
        content_func: Function that renders the card content
        icon: Optional icon for the title
        expanded: Whether the card is expanded by default
        key: Unique key for the expander (ignored in current version)
    """
    display_title = f"{icon} {title}" if icon else title
    
    with st.expander(display_title, expanded=expanded):
        content_func()


def create_info_card():
    """Create the main recipe info card"""
    # This will be shown as the main title, not in a card
    pass


def create_photos_card(recette):
    """Create the photos card"""
    from streamlit_app.utils.photo_components import enhanced_photo_viewer
    
    def photos_content():
        enhanced_photo_viewer(recette)
    
    create_card(
        title="Photos", 
        content_func=photos_content,
        icon="📷",
        expanded=True,
        key=f"photos_card_{recette.id}"
    )


def create_categories_card(recette):
    """Create the categories card"""
    def categories_content():
        if recette.categories:
            # Create colored badges for categories
            categories_text = ""
            for cat in recette.categories:
                categories_text += f"🏷️ **{cat.nom}** "
            st.markdown(categories_text)
        else:
            st.info("Aucune catégorie définie")
    
    create_card(
        title="Catégories",
        content_func=categories_content,
        icon="🏷️",
        expanded=True,
        key=f"categories_card_{recette.id}"
    )


def create_ingredients_card(recette):
    """Create the ingredients card"""
    def ingredients_content():
        if not recette.ingredients:
            st.info("Aucun ingrédient défini")
            return
            
        for ing in recette.ingredients:
            # Create ingredient line with formatting
            ligne = f"• **{ing.nom}** : {ing.quantite} {ing.unite or ''}"
            
            if ing.indispensable:
                st.markdown(f"{ligne} 🟢")
            elif ing.alternatives:
                st.markdown(f"{ligne} 🔄")
                st.caption(f"Alternatives : {ing.alternatives}")
            else:
                st.markdown(ligne)
    
    create_card(
        title="Ingrédients",
        content_func=ingredients_content,
        icon="🧂",
        expanded=True,
        key=f"ingredients_card_{recette.id}"
    )


def create_steps_card(recette):
    """Create the cooking steps card"""
    def steps_content():
        if not recette.etapes:
            st.info("Aucune étape définie")
            return
            
        # Sort steps by order
        etapes_sorted = sorted(recette.etapes, key=lambda e: e.ordre)
        
        for etape in etapes_sorted:
            st.markdown(f"**{etape.ordre}.** {etape.description}")
    
    create_card(
        title="Étapes de préparation",
        content_func=steps_content,
        icon="📝",
        expanded=True,
        key=f"steps_card_{recette.id}"
    )


def create_metrics_card(recette):
    """Create the metrics/timing card"""
    def metrics_content():
        col1, col2, col3 = st.columns(3)
        
        with col1:
            st.metric(
                label="⏱️ Préparation",
                value=f"{recette.preparation or 0} min"
            )
        
        with col2:
            st.metric(
                label="🔥 Cuisson", 
                value=f"{recette.cuisson or 0} min"
            )
        
        with col3:
            st.metric(
                label="👥 Portions",
                value=recette.portions or "N/A"
            )
        
        # Total time
        total_time = (recette.preparation or 0) + (recette.cuisson or 0)
        if total_time > 0:
            st.markdown(f"**⏰ Temps total : {total_time} min**")
    
    create_card(
        title="Informations de cuisson",
        content_func=metrics_content,
        icon="⏱️",
        expanded=True,
        key=f"metrics_card_{recette.id}"
    )


def create_tags_card(recette):
    """Create the tags card"""
    def tags_content():
        if recette.tags:
            tags_text = ""
            for tag in recette.tags:
                tags_text += f"🔖 **{tag.nom}** "
            st.markdown(tags_text)
        else:
            st.info("Aucun tag défini")
    
    create_card(
        title="Tags",
        content_func=tags_content,
        icon="🔖",
        expanded=False,  # Less important, collapsed by default
        key=f"tags_card_{recette.id}"
    )


def create_source_card(recette):
    """Create the source information card"""
    def source_content():
        if not recette.source:
            st.info("Aucune source définie")
            return
        
        source = recette.source
        
        if source.type == "homemade":
            st.success("🏠 **Recette maison** - Création originale")
        elif source.type == "url" and source.url:
            st.markdown(f"🌐 **Source web :** [{source.url}]({source.url})")
        elif source.type == "book":
            livre_info = []
            if source.book_title:
                livre_info.append(f"📖 **{source.book_title}**")
            if source.book_authors:
                livre_info.append(f"✍️ {source.book_authors}")
            if source.book_page:
                livre_info.append(f"📄 Page {source.book_page}")
            
            if livre_info:
                st.markdown(" | ".join(livre_info))
            else:
                st.info("📚 **Source livre** - Informations incomplètes")
    
    create_card(
        title="Source",
        content_func=source_content,
        icon="📚",
        expanded=False,  # Less important, collapsed by default
        key=f"source_card_{recette.id}"
    )