import React from "react";
import { View, Text, StyleSheet, Image } from "react-native";

const Payments = () => {
  return (
    <View style={styles.container}>
      {/* <Image
        source={{
          uri: "https://images.unsplash.com/photo-1519389950473-47ba0277781c",
        }}
        style={styles.image}
        resizeMode="contain"
      /> */}
      <Text style={styles.title}>Payments</Text>
      <Text style={styles.subtitle}>
        Here you can manage your payment methods and view transaction history.
      </Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: "#f9f9f9",
    justifyContent: "center",
    alignItems: "center",
  },
  image: {
    width: 180,
    height: 180,
    marginBottom: 20,
    borderRadius: 15,
  },
  title: {
    fontSize: 28,
    fontWeight: "700",
    color: "#2c3e50",
    marginBottom: 12,
    textAlign: "center",
  },
  subtitle: {
    fontSize: 16,
    color: "#7f8c8d",
    textAlign: "center",
    lineHeight: 22,
  },
});

export default Payments;
