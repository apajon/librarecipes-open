import streamlit as st

st.title("Test Minimal LibraRecipes")
st.write("Si vous voyez ce texte, Streamlit fonctionne !")
st.success("✅ Test minimal réussi")

if st.button("Test bouton"):
    st.balloons()
    st.write("Le bouton fonctionne !")
